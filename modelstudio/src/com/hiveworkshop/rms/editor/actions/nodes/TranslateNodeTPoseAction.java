package com.hiveworkshop.rms.editor.actions.nodes;

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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class TranslateNodeTPoseAction extends AbstractTransformAction {
	private final ModelStructureChangeListener changeListener;
	private final IdObject node;
	private final Vec3 oldPivot;
	private final Vec3 newPivot;
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();
	private final Vec3 tempTransl = new Vec3();
	private final Vec3 tempDelta = new Vec3();
	private final AddTimelineAction<?> timelineAction;
	private final Vec3AnimFlag newTranslation;
	private final Map<IdObject, Vec3> childToOrgLoc = new LinkedHashMap<>();
	private final Map<IdObject, Vec3> childToNewLoc = new LinkedHashMap<>();

	public TranslateNodeTPoseAction(IdObject node,
	                                Vec3 translation,
	                                Mat4 rotMat,
	                                ModelStructureChangeListener changeListener){
		this.changeListener = changeListener;
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
		Vec3 pivotPoint = node.getPivotPoint();
		this.node = node;
		this.oldPivot = new Vec3(pivotPoint);
		this.newPivot = new Vec3(pivotPoint);

		for (IdObject child : node.getChildrenNodes()) {
			collectPivots(child);
		}
		newTranslation = getTimeline();
		if (newTranslation != null) {
			timelineAction = new AddTimelineAction<>(node, newTranslation);
		} else {
			timelineAction = null;
		}
		move(setTranslationHeap(pivotPoint, translation));
	}

	private void collectPivots(IdObject node){
		childToOrgLoc.put(node, new Vec3(node.getPivotPoint()));
		childToNewLoc.put(node, new Vec3(node.getPivotPoint()));
		for (IdObject child : node.getChildrenNodes()) {
			collectPivots(child);
		}
	}

	private Vec3AnimFlag getTimeline() {
		AnimFlag<?> translation = node.find(MdlUtils.TOKEN_TRANSLATION);
		if (translation instanceof Vec3AnimFlag) {
			return  (Vec3AnimFlag)translation.deepCopy();
		}
		return null;
	}

	public TranslateNodeTPoseAction doSetup() {
		node.setPivotPoint(newPivot);
		for (IdObject idObject : childToNewLoc.keySet()) {
			idObject.setPivotPoint(childToNewLoc.get(idObject));
		}
		if (timelineAction != null) {
			timelineAction.redo();
		}
		return this;
	}

	public TranslateNodeTPoseAction updateTranslation(Vec3 delta){
		move(setTranslationHeap(node.getPivotPoint(), delta));
		node.setPivotPoint(newPivot);
		for (IdObject idObject : childToNewLoc.keySet()) {
			idObject.setPivotPoint(childToNewLoc.get(idObject));
		}
		return this;
	}

	private void move(Vec3 delta) {
		newPivot.add(delta);
		for (Vec3 loc : childToNewLoc.values()) {
			loc.add(delta);
		}
		tempDelta.set(Vec3.ZERO).sub(delta);

		if (newTranslation != null) {
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
		if (timelineAction != null) {
			timelineAction.undo();
		}
		for (IdObject idObject : childToOrgLoc.keySet()) {
			idObject.setPivotPoint(childToOrgLoc.get(idObject));
		}
		node.setPivotPoint(oldPivot);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public TranslateNodeTPoseAction redo() {
		node.setPivotPoint(newPivot);
		for (IdObject idObject : childToNewLoc.keySet()) {
			idObject.setPivotPoint(childToNewLoc.get(idObject));
		}
		if (timelineAction != null) {
			timelineAction.redo();
		}
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Rotate " + node.getName();
	}

	private Vec3 setTranslationHeap(Vec3 pivot, Vec3 newDelta) {
		tempTransl.set(pivot)
				.transform(rotMat, 1, true)
				.add(newDelta)
				.transform(invRotMat, 1, true)
				.sub(pivot);
		return tempTransl;
	}

}
