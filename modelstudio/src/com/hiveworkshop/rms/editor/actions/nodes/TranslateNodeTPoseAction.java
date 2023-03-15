package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.AddTimelineAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class TranslateNodeTPoseAction extends AbstractTransformAction {
	private final ModelStructureChangeListener changeListener;
	private final IdObject node;
	private final Vec3 oldPivot;
	private final Vec3 newPivot;
	private final Vec3 translation;
	private final Vec3 translation2 = new Vec3();
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();
	private final List<AddTimelineAction<?>> timelineActions = new ArrayList<>();
	private final List<Vec3AnimFlag> newTranslations = new ArrayList<>();
//	private final List<Vec3AnimFlag> newScalings = new ArrayList<>();
//	private final List<QuatAnimFlag> newRotations = new ArrayList<>();
	private final List<RotateNodeChildTPoseAction> rotChildActions = new ArrayList<>();
	private final Map<IdObject, Vec3> childToOrgLoc = new LinkedHashMap<>();
	private final Map<IdObject, Vec3> childToNewLoc = new LinkedHashMap<>();

	public TranslateNodeTPoseAction(IdObject node,
	                                Vec3 translation,
	                                Mat4 rotMat,
	                                ModelStructureChangeListener changeListener){
		this.changeListener = changeListener;
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
		this.translation = new Vec3(setTranslationHeap(node, translation));
		this.node = node;
		this.oldPivot = new Vec3(node.getPivotPoint());
		this.newPivot = new Vec3(node.getPivotPoint());

		translation2.sub(translation);
		for(IdObject child : node.getChildrenNodes()){
			collectLocs(child, this.translation);
		}
		collectTimelines();
		createTimelineActions();
		move(translation);
	}

	private void collectLocs(IdObject node, Vec3 delta){
		System.out.println("\t collecting node: " + node.getName());
		childToOrgLoc.put(node, new Vec3(node.getPivotPoint()));
		childToNewLoc.put(node, new Vec3(node.getPivotPoint()).add(delta));
		for(IdObject child : node.getChildrenNodes()){
			collectLocs(child, delta);
		}
	}

	private void collectTimelines() {
		AnimFlag<?> translation = node.find(MdlUtils.TOKEN_TRANSLATION);
		if (translation instanceof Vec3AnimFlag) {
			newTranslations.add((Vec3AnimFlag)translation.deepCopy());
		}
	}

	public TranslateNodeTPoseAction doSetup() {
		node.setPivotPoint(newPivot);
		for (IdObject idObject : childToNewLoc.keySet()){
			idObject.setPivotPoint(childToNewLoc.get(idObject));
		}
		for(UndoAction action : timelineActions){
			action.redo();
		}
		return this;
	}

	private void createTimelineActions() {
		if(timelineActions.isEmpty()){
			for (Vec3AnimFlag newTranslation : newTranslations){
				timelineActions.add(new AddTimelineAction<>(node, newTranslation));
			}
		}
	}


	public TranslateNodeTPoseAction updateTranslation(Vec3 delta){
//		System.out.println("moving: " + delta);
		move(setTranslationHeap(node, delta));
		node.setPivotPoint(newPivot);
		for (IdObject idObject : childToNewLoc.keySet()){
			idObject.setPivotPoint(childToNewLoc.get(idObject));
		}
		return this;
	}

	Vec3 tempDelta = new Vec3();
	private void move(Vec3 delta) {
//		System.out.println("moving " + node.getName() + " " + delta);
		newPivot.add(delta);
		for (Vec3 loc : childToNewLoc.values()){
			loc.add(delta);
		}
		tempDelta.set(Vec3.ZERO).sub(delta);
		updateTimelines(tempDelta);
	}

	private void updateTimelines(Vec3 delta) {
		for (Vec3AnimFlag newTranslation : newTranslations){
			moveTranslations(delta, newTranslation);
		}
	}

	private void moveTranslations(Vec3 dist, Vec3AnimFlag newTranslation) {
		for (TreeMap<Integer, Entry<Vec3>> entryMap : newTranslation.getAnimMap().values()) {
			if (entryMap != null) {
				for (Entry<Vec3> entry : entryMap.values()) {
					entry.getValue().add(dist);
					if (newTranslation.tans()) {
						entry.getInTan().add(dist);
						entry.getOutTan().add(dist);
					}
				}
			}
		}
	}

	@Override
	public TranslateNodeTPoseAction undo() {
		for(UndoAction action : timelineActions){
			action.undo();
		}
		for (IdObject idObject : childToOrgLoc.keySet()){
			idObject.setPivotPoint(childToOrgLoc.get(idObject));
		}
		node.setPivotPoint(oldPivot);
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public TranslateNodeTPoseAction redo() {
		node.setPivotPoint(newPivot);
		for (IdObject idObject : childToNewLoc.keySet()){
			idObject.setPivotPoint(childToNewLoc.get(idObject));
		}
		for(UndoAction action : timelineActions){
			action.redo();
		}
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Rotate " + node.getName();
	}


	Vec3 tempVec = new Vec3();

	private Vec3 setTranslationHeap(IdObject idObject, Vec3 newDelta) {
		tempVec.set(idObject.getPivotPoint())
				.transform(rotMat, 1, true)
				.add(newDelta)
				.transform(invRotMat, 1, true)
				.sub(idObject.getPivotPoint());

		return tempVec;
	}

}
