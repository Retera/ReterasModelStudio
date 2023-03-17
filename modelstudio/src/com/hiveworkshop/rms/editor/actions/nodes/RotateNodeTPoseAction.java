package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.animation.AddTimelineAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class RotateNodeTPoseAction extends AbstractTransformAction {
	private final ModelStructureChangeListener changeListener;
	private final IdObject node;
	private final Vec3 oldPivot;
	private final Vec3 newPivot;
	private double radians;
	private final Quat quat = new Quat();
	private final Vec3 axis;
	private final Vec3 center;
	private final Mat4 rotMat = new Mat4();
	private final QuatAnimFlag newRotation;
	private final AddTimelineAction<?> timelineAction;
	private final List<RotateNodeChildTPoseAction> rotChildActions = new ArrayList<>();

	public RotateNodeTPoseAction(IdObject node,
	                             Quat quat, Vec3 center,
	                             Mat4 rotMat,
	                             ModelStructureChangeListener changeListener){
		this(node, quat.getAxis(), quat.getAxisAngle(), center, rotMat, changeListener);
	}

	public RotateNodeTPoseAction(IdObject node,
	                             Vec3 axis, double radians, Vec3 center,
	                             Mat4 rotMat,
	                             ModelStructureChangeListener changeListener){
		this.changeListener = changeListener;
		this.rotMat.set(rotMat);
		this.radians = radians;
		this.axis = axis;
		this.center = center;
		this.node = node;
		this.oldPivot = new Vec3(node.getPivotPoint());
		quat.setFromAxisAngle(axis, (float) radians);
		this.newPivot = new Vec3(node.getPivotPoint()).rotate(center, quat);

		for (IdObject child : node.getChildrenNodes()) {
			collectRotActions(child, node.getPivotPoint(), axis, -radians);
		}
		newRotation = getTimeline();
		if (newRotation != null) {
			timelineAction = new AddTimelineAction<>(node, newRotation);
		} else {
			timelineAction = null;
		}
		quat.setIdentity().mulInverse(new Quat(axis, (float) radians)).normalize();
		rotate(quat.getAxisAngle());
	}

	private void collectRotActions(IdObject node, Vec3 pivot, Vec3 axis, double radians) {
		rotChildActions.add(new RotateNodeChildTPoseAction(node, axis, radians, pivot, rotMat, null));
		for (IdObject child : node.getChildrenNodes()) {
			collectRotActions(child, pivot, axis, radians);
		}
	}

	private QuatAnimFlag getTimeline() {
		AnimFlag<?> rotation = node.find(MdlUtils.TOKEN_ROTATION);
		if (rotation instanceof QuatAnimFlag) {
			return  (QuatAnimFlag) rotation.deepCopy();
		}
		return null;
	}

	public RotateNodeTPoseAction doSetup() {
		node.setPivotPoint(newPivot);
		for (RotateNodeChildTPoseAction action : rotChildActions) {
			action.redo();
		}
		if (timelineAction != null) {
			timelineAction.redo();
		}
		return this;
	}

	public RotateNodeTPoseAction updateRotation(double radians){
		this.radians += radians;
		for (RotateNodeChildTPoseAction action : rotChildActions) {
			action.updateRotation(radians);
		}
		rotate(-radians);
		node.setPivotPoint(newPivot);
		return this;
	}
	public RotateNodeTPoseAction setRotation(double radians){
		double rotDiff = radians - this.radians;
		this.radians = radians;
		for (RotateNodeChildTPoseAction action : rotChildActions) {
			action.setRotation(radians);
		}
		rotate(-rotDiff);
		node.setPivotPoint(newPivot);
		return this;
	}

	private void rotate(double radians) {
		quat.setFromAxisAngle(axis, (float) radians);
		newPivot.rotate(center, quat);
		quat.setFromAxisAngle(axis, (float) -radians);
		if(newRotation != null){
			rotRotations(quat, newRotation);
		}
	}

	private void rotTranslations(Quat quat, Vec3AnimFlag newTranslation) {
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

	private void rotScalings(Quat quat, Vec3 tempScaleMul, Vec3AnimFlag newScaling) {
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

	private void rotRotations(Quat quat, QuatAnimFlag newRotation) {
		for (TreeMap<Integer, Entry<Quat>> entryMap : newRotation.getAnimMap().values()) {
			if (entryMap != null) {
				for (Entry<Quat> entry : entryMap.values()) {
					entry.getValue().mul(quat);
					if (newRotation.tans()) {
						entry.getInTan().mul(quat);
						entry.getOutTan().mul(quat);
					}
				}

			}
		}
	}

	@Override
	public RotateNodeTPoseAction undo() {
		if (timelineAction != null) {
			timelineAction.undo();
		}
		for (RotateNodeChildTPoseAction action : rotChildActions) {
			action.undo();
		}
		node.setPivotPoint(oldPivot);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public RotateNodeTPoseAction redo() {
		node.setPivotPoint(newPivot);
		for (RotateNodeChildTPoseAction action : rotChildActions) {
			action.redo();
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
}
