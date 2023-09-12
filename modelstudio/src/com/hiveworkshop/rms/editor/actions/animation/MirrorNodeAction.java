package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class MirrorNodeAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final IdObject node;
	private final Vec3 oldPivot;
	private final Vec3 newPivot;
	private final Vec3 mirrorAxis;
	private final Vec3 mirrorPoint;
	private final List<AddTimelineAction<?>> timelineActions = new ArrayList<>();

	public MirrorNodeAction(IdObject node,
	                        Vec3 mirrorAxis, Vec3 mirrorPoint,
	                        ModelStructureChangeListener changeListener){
		this.changeListener = changeListener;
		this.mirrorAxis = new Vec3(mirrorAxis).normalize();
		this.mirrorPoint = mirrorPoint;
		this.node = node;
		this.oldPivot = new Vec3(node.getPivotPoint());
		Vec3 tempMirror = new Vec3(Vec3.ONE).addScaled(mirrorAxis, -2f);
		this.newPivot = new Vec3(node.getPivotPoint()).scale(mirrorPoint, tempMirror);

		collectTimelineActions(node, tempMirror);
	}

	public void collectTimelineActions(AnimatedNode node, Vec3 scale) {
		Vec3AnimFlag translation = (Vec3AnimFlag) node.find(MdlUtils.TOKEN_TRANSLATION);
		if (translation != null) {
			AnimFlag<Vec3> newTranslation = translation.deepCopy();
			for (TreeMap<Integer, Entry<Vec3>> entryMap : newTranslation.getAnimMap().values()) {
				if (entryMap != null) {
					for (Entry<Vec3> entry : entryMap.values()) {
						entry.getValue().multiply(scale);
						if (newTranslation.tans()) {
							entry.getInTan().multiply(scale);
							entry.getOutTan().multiply(scale);
						}
					}
				}
			}
			timelineActions.add(new AddTimelineAction<>(node, newTranslation));
		}
		Vec4 temp = new Vec4();
		Vec4 axisScale = new Vec4(scale, -1);
		QuatAnimFlag rotation = (QuatAnimFlag) node.find(MdlUtils.TOKEN_ROTATION);
		if (rotation != null) {
			AnimFlag<Quat> newRotation = rotation.deepCopy();
			for (TreeMap<Integer, Entry<Quat>> entryMap : newRotation.getAnimMap().values()) {
				if (entryMap != null) {
					for (Entry<Quat> entry : entryMap.values()) {
						temp.setAsAxisWithAngle(entry.getValue()).multiply(axisScale);
						entry.getValue().setFromAxisAngle(temp);
						if (newRotation.tans()) {
							temp.setAsAxisWithAngle(entry.getInTan()).multiply(axisScale);
							entry.getInTan().setFromAxisAngle(temp);
							temp.setAsAxisWithAngle(entry.getOutTan()).multiply(axisScale);
							entry.getOutTan().setFromAxisAngle(temp);
						}
					}
				}
			}
			timelineActions.add(new AddTimelineAction<>(node, newRotation));
		}
	}

	@Override
	public UndoAction undo() {
		for(UndoAction action : timelineActions){
			action.undo();
		}
		node.setPivotPoint(oldPivot);
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		node.setPivotPoint(newPivot);
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
		return "Mirror " + node.getName();
	}
}
