package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class RotateNodeAction extends AbstractTransformAction {
	private final ModelStructureChangeListener changeListener;
	private final IdObject node;
	private final Vec3 oldPivot;
	private final Vec3 newPivot;
	private final Quat quat = new Quat();
	private final Vec3 axis;
	private final Vec3 center;
	private final List<AddTimelineAction<?>> timelineActions = new ArrayList<>();
	private final List<Vec3AnimFlag> newTranslations = new ArrayList<>();
	private final List<Vec3AnimFlag> newScalings = new ArrayList<>();
	private final List<QuatAnimFlag> newRotations = new ArrayList<>();

	public RotateNodeAction(IdObject node,
	                        Vec3 axis, double radians, Vec3 center,
	                        ModelStructureChangeListener changeListener){
		this.changeListener = changeListener;
		this.axis = axis;
		this.center = center;
		this.node = node;
		this.oldPivot = new Vec3(node.getPivotPoint());
		this.newPivot = new Vec3(node.getPivotPoint()).rotate(center, quat);

		collectTimelines();
		createTimelineActions();
		rotate(radians);
	}

	private void collectTimelines() {
		AnimFlag<?> translation = node.find(MdlUtils.TOKEN_TRANSLATION);
		if (translation instanceof Vec3AnimFlag) {
			newTranslations.add((Vec3AnimFlag)translation.deepCopy());
		}
		AnimFlag<?> scaling = node.find(MdlUtils.TOKEN_SCALING);
		if (scaling instanceof Vec3AnimFlag) {
			newScalings.add((Vec3AnimFlag)scaling.deepCopy());
		}
		AnimFlag<?> rotation = node.find(MdlUtils.TOKEN_ROTATION);
		if (rotation instanceof QuatAnimFlag) {
			newRotations.add((QuatAnimFlag) rotation.deepCopy());
		}
	}

	public RotateNodeAction doSetup() {
		node.setPivotPoint(newPivot);
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
			for (Vec3AnimFlag newScaling : newScalings){
				timelineActions.add(new AddTimelineAction<>(node, newScaling));
			}
			for (QuatAnimFlag newRotation : newRotations){
				timelineActions.add(new AddTimelineAction<>(node, newRotation));
			}
		}
	}

	public RotateNodeAction updateRotation(double radians){
		rotate(radians);
		node.setPivotPoint(newPivot);
		return this;
	}

	private void rotate(double radians) {
		quat.setFromAxisAngle(axis, (float) -radians);
		newPivot.rotate(center, quat);
		updateTimelines(radians);
	}

	private void updateTimelines(double radians) {
		quat.setFromAxisAngle(axis, (float) -radians);
		for (Vec3AnimFlag newTranslation : newTranslations){
			for (TreeMap<Integer, Entry<Vec3>> entryMap : newTranslation.getAnimMap().values()) {
				if (entryMap != null) {
					for (Entry<Vec3> entry : entryMap.values()) {
						entry.getValue().rotate(Vec3.ZERO, quat);
						if (newTranslation.tans()) {
							entry.getInTan().rotate(Vec3.ZERO, quat);
							entry.getOutTan().rotate(Vec3.ZERO, quat);
						}
					}
				}
			}
		}
		Vec3 tempScaleMul = new Vec3(Vec3.ONE).rotate(Vec3.ZERO, quat);
		for (Vec3AnimFlag newScaling : newScalings){
			for (TreeMap<Integer, Entry<Vec3>> entryMap : newScaling.getAnimMap().values()) {
				if (entryMap != null) {
					for (Entry<Vec3> entry : entryMap.values()) {
						entry.getValue().rotate(Vec3.ZERO, quat).multiply(tempScaleMul);
						if (newScaling.tans()) {
							entry.getInTan().rotate(Vec3.ZERO, quat).multiply(tempScaleMul);
							entry.getOutTan().rotate(Vec3.ZERO, quat).multiply(tempScaleMul);
						}
					}
				}
			}
		}
		Vec3 tempAxis = new Vec3();
		for (QuatAnimFlag newRotation : newRotations){
			for (TreeMap<Integer, Entry<Quat>> entryMap : newRotation.getAnimMap().values()) {
				if (entryMap != null) {
					for (Entry<Quat> entry : entryMap.values()) {
						tempAxis.setAsAxis(entry.getValue());
						float axisAngle = entry.getValue().getAxisAngle();
						tempAxis.rotate(Vec3.ZERO, quat);
						entry.getValue().setFromAxisAngle(tempAxis, axisAngle);
						if (newRotation.tans()) {
							tempAxis.setAsAxis(entry.getInTan());
							axisAngle = entry.getInTan().getAxisAngle();
							tempAxis.rotate(Vec3.ZERO, quat);
							entry.getInTan().setFromAxisAngle(tempAxis, axisAngle);

							tempAxis.setAsAxis(entry.getOutTan());
							axisAngle = entry.getOutTan().getAxisAngle();
							tempAxis.rotate(Vec3.ZERO, quat);
							entry.getOutTan().setFromAxisAngle(tempAxis, axisAngle);
						}
					}

				}
			}
		}
	}

	@Override
	public RotateNodeAction undo() {
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
	public RotateNodeAction redo() {
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
		return "Rotate " + node.getName();
	}
}
