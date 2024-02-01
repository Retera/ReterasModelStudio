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
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
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
	private double radians;
	private final Vec3 axis;
	private final Vec3 center;
	private final List<AddTimelineAction<?>> timelineActions = new ArrayList<>();
	private Vec3AnimFlag newTranslation;
	private Vec3AnimFlag newScaling;
	private QuatAnimFlag newRotation;
	private Vec3AnimFlag orgTranslation;
	private Vec3AnimFlag orgScaling;
	private QuatAnimFlag orgRotation;

	public RotateNodeAction(IdObject node,
	                        Vec3 axis, double radians, Vec3 center,
	                        ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.radians = radians;
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
			orgTranslation = (Vec3AnimFlag) translation;
			newTranslation = (Vec3AnimFlag) orgTranslation.deepCopy();
		}
		AnimFlag<?> scaling = node.find(MdlUtils.TOKEN_SCALING);
		if (scaling instanceof Vec3AnimFlag) {
			orgScaling = (Vec3AnimFlag) scaling;
			newScaling = (Vec3AnimFlag) orgScaling.deepCopy();
		}
		AnimFlag<?> rotation = node.find(MdlUtils.TOKEN_ROTATION);
		if (rotation instanceof QuatAnimFlag) {
			orgRotation = (QuatAnimFlag) rotation;
			newRotation = (QuatAnimFlag) orgRotation.deepCopy();
		}
	}

	public RotateNodeAction doSetup() {
		node.setPivotPoint(newPivot);
		for (UndoAction action : timelineActions) {
			action.redo();
		}
		return this;
	}

	private void createTimelineActions() {
		if (timelineActions.isEmpty()) {
			if (newTranslation != null) timelineActions.add(new AddTimelineAction<>(node, newTranslation));
			if (newScaling     != null) timelineActions.add(new AddTimelineAction<>(node, newScaling));
			if (newRotation    != null) timelineActions.add(new AddTimelineAction<>(node, newRotation));
		}
	}

	public RotateNodeAction setRotation(double radians) {
		this.radians = radians;
		rotate(this.radians);
		node.setPivotPoint(newPivot);
		return this;
	}

	public RotateNodeAction updateRotation(double radians) {
		this.radians += radians;
		rotate(this.radians);
		node.setPivotPoint(newPivot);
		return this;
	}

	private void rotate(double radians) {
		quat.setFromAxisAngle(axis, (float) -radians);
		newPivot.set(oldPivot).rotate(center, quat);
		updateTimelines(radians);
	}

	private Vec3[] getVecCompAxis() {
		Quat rotTot = new Quat().rotZ(((float) Math.PI/2.0f)).rotY(((float) Math.PI/2.0f));
		Vec3 vAxis = new Vec3(axis).transform(rotTot);
		Vec3 crossV = new Vec3(axis).cross(vAxis);
		if (.7 < crossV.length()) {
			Vec3 axis2 = new Vec3(axis).cross(crossV).scale(-1f);
			return new Vec3[] {crossV, axis2};
		}

		Vec3 crossX = new Vec3(axis).cross(Vec3.X_AXIS);
		if (.7 < crossX.length()) {
			Vec3 axis2 = new Vec3(axis).cross(crossX).scale(-1f);
			System.out.println("  crossX: " + crossX + ", axis2: " + axis2);
			return new Vec3[] {crossX, axis2};
		}
		Vec3 crossY = new Vec3(axis).cross(Vec3.Y_AXIS);
		if (.7 < crossY.length()) {
			Vec3 axis2 = new Vec3(axis).cross(crossY).scale(-1f);
			System.out.println("  crossY: " + crossY + ", axis2: " + axis2);
			return new Vec3[] {crossY, axis2};
		}
		Vec3 crossZ = new Vec3(axis).cross(Vec3.Z_AXIS);
		Vec3 axis2 = new Vec3(axis).cross(crossZ).scale(-1f);
		System.out.println("  crossZ: " + crossZ + ", axis2: " + axis2);
		return new Vec3[] {crossZ, axis2};
	}

	private void updateTimelines(double radians) {
		quat.setFromAxisAngle(axis, (float) -radians);
		if (newTranslation != null) {
			for (Sequence sequence : newTranslation.getAnimMap().keySet()) {
				TreeMap<Integer, Entry<Vec3>> entryMap = newTranslation.getEntryMap(sequence);
				if (entryMap != null) {
					for (Entry<Vec3> entry : entryMap.values()) {
						entry.setValues(orgTranslation.getEntryAt(sequence, entry.getTime()));
						entry.getValue().rotate(Vec3.ZERO, quat);
						if (entry.isTangential()) {
							entry.getInTan().rotate(Vec3.ZERO, quat);
							entry.getOutTan().rotate(Vec3.ZERO, quat);
						}
					}
				}
			}
		}

		Vec3 tempAxis = new Vec3();
		if (newRotation != null) {
			for (Sequence sequence : newRotation.getAnimMap().keySet()) {
				TreeMap<Integer, Entry<Quat>> entryMap = newRotation.getEntryMap(sequence);
				if (entryMap != null) {
					for (Entry<Quat> entry : entryMap.values()) {
						entry.setValues(orgRotation.getEntryAt(sequence, entry.getTime()));
						tempAxis.setAsAxis(entry.getValue());
						float axisAngle = entry.getValue().getAxisAngle();
						tempAxis.rotate(Vec3.ZERO, quat);
						entry.getValue().setFromAxisAngle(tempAxis, axisAngle);
						if (entry.isTangential()) {
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

		quat.setFromAxisAngle(axis, (float) -radians);

		double deg90 = (Math.PI / 2d);
		double deg0To180 = ((radians % Math.PI) + Math.PI) % Math.PI;
		if (deg90 < deg0To180) {
			deg0To180 = Math.PI - deg0To180;
		}

		Vec3[] vecCompAxis = getVecCompAxis();
		if (newScaling != null) {
			for (Sequence sequence : newScaling.getAnimMap().keySet()) {
				TreeMap<Integer, Entry<Vec3>> entryMap = newScaling.getEntryMap(sequence);
				if (entryMap != null) {
					for (Entry<Vec3> entry : entryMap.values()) {
						entry.setValues(orgScaling.getEntryAt(sequence, entry.getTime()));

						rotScaleValue(deg0To180, vecCompAxis, entry.getValue());

						if (entry.isTangential()) {
							rotScaleValue(deg0To180, vecCompAxis, entry.getInTan());
							rotScaleValue(deg0To180, vecCompAxis, entry.getOutTan());
						}
					}
				}
			}
		}
	}

	private final Vec3 tempScaleCenter = new Vec3();
	private final Vec3 tempScaleV = new Vec3();
	private void rotScaleValue(double deg0To180, Vec3[] vecCompAxis, Vec3 value) {
		float v1 = value.dot(vecCompAxis[0]);
		float v2 = value.dot(vecCompAxis[1]);
		float smallestAxis = Math.min(Math.abs(v1), Math.abs(v2));
		tempScaleCenter.set(Math.copySign(smallestAxis, value.x), Math.copySign(smallestAxis, value.y), Math.copySign(smallestAxis, value.z));

		quat.setFromAxisAngle(axis, (float) -deg0To180);
		tempScaleV.set(value).rotate(tempScaleCenter, quat);
		quat.setFromAxisAngle(axis, (float) deg0To180);
		value.rotate(tempScaleCenter, quat);
		if (value.length() < tempScaleV.length()) {
			value.set(tempScaleV);
		}
	}


	@Override
	public RotateNodeAction undo() {
		for (UndoAction action : timelineActions) {
			action.undo();
		}
		node.setPivotPoint(oldPivot);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public RotateNodeAction redo() {
		node.setPivotPoint(newPivot);
		for (UndoAction action : timelineActions) {
			action.redo();
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
}
