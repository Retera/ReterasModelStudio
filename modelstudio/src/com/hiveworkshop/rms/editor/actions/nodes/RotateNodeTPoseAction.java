package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.animation.AddTimelineAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
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
	private final AnimFlag<Quat> newRotation;
	private final AnimFlag<Vec3> newTranslation;
	private AddTimelineAction<Quat> rotTimelineAction;
	private AddTimelineAction<Vec3> translTimelineAction;
	private final List<RotateNodeChildTPoseAction> rotChildActions = new ArrayList<>();
	boolean preserveAnimations;

	public RotateNodeTPoseAction(IdObject node,
	                             Vec3 axis, double radians, Vec3 center,
	                             Mat4 rotMat,
	                             boolean preserveAnimations,
	                             AnimFlag<Quat> newRotation,
	                             AnimFlag<Vec3> newTranslation,
	                             ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.preserveAnimations = preserveAnimations;
		this.rotMat.set(rotMat);
		this.radians = radians;
		this.axis = axis;
		this.center = center;
		this.node = node;
		this.oldPivot = new Vec3(node.getPivotPoint());
		quat.setFromAxisAngle(axis, (float) radians);
		this.newPivot = new Vec3(node.getPivotPoint()).rotate(center, quat);

		this.newRotation = newRotation;
		if (newRotation != null) {
			rotTimelineAction = new AddTimelineAction<>(node, newRotation);
		}
		this.newTranslation = newTranslation;
		if (newTranslation != null) {
			translTimelineAction = new AddTimelineAction<>(node, newTranslation);
		}

		for (IdObject child : node.getChildrenNodes()) {
			collectRotActions(child, center, axis, -radians);
		}
		quat.setIdentity().mulInverse(new Quat(axis, (float) radians)).normalize();
		rotate(-quat.getAxisAngle());
	}

	private void collectRotActions(IdObject node, Vec3 pivot, Vec3 axis, double radians) {
		rotChildActions.add(new RotateNodeChildTPoseAction(node, axis, radians, pivot, rotMat, null));
		for (IdObject child : node.getChildrenNodes()) {
			collectRotActions(child, pivot, axis, radians);
		}
	}

	public RotateNodeTPoseAction doSetup() {
		node.setPivotPoint(newPivot);
		for (RotateNodeChildTPoseAction action : rotChildActions) {
			action.redo();
		}
		if (rotTimelineAction != null) {
			rotTimelineAction.redo();
		}
		if (translTimelineAction != null) {
			translTimelineAction.redo();
		}
		return this;
	}

	public RotateNodeTPoseAction updateRotation(double radians) {
		this.radians += radians;
		for (RotateNodeChildTPoseAction action : rotChildActions) {
			action.updateRotation(radians);
		}
		rotate(radians);
		node.setPivotPoint(newPivot);
		return this;
	}
	public RotateNodeTPoseAction setRotation(double radians) {
		double rotDiff = radians - this.radians;
		this.radians = radians;
		for (RotateNodeChildTPoseAction action : rotChildActions) {
			action.setRotation(radians);
		}
		rotate(rotDiff);
		node.setPivotPoint(newPivot);
		return this;
	}

	private final Vec3 tempDelta = new Vec3();
	private void rotate(double radians) {
		quat.setFromAxisAngle(axis, (float) -radians);

		newPivot.set(node.getPivotPoint());
		tempDelta.set(newPivot);
		newPivot.rotate(center, quat);
		tempDelta.sub(newPivot);

		if (newTranslation != null) {
			moveTranslations(tempDelta, newTranslation);
		}
		quat.setFromAxisAngle(axis, (float) radians);
		if (newRotation != null) {
			rotRotations(quat, newRotation);
		}
	}

	private void moveTranslations(Vec3 dist, AnimFlag<Vec3> newTranslation) {
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

	private void rotRotations(Quat quat, AnimFlag<Quat> newRotation) {
//		rotRotationsRotAxis(quat, newRotation);
		rotRotationsQuatMul(quat, newRotation);
	}
	private void rotRotationsRotAxis(Quat quat, AnimFlag<Quat> newRotation) {
		for (TreeMap<Integer, Entry<Quat>> entryMap : newRotation.getAnimMap().values()) {
			if (entryMap != null) {
				for (Entry<Quat> entry : entryMap.values()) {
//					if(entry.time == 0) {
//						Quat tempQ1 = new Quat(entry.getValue()).rotateAxis(quat);
//						Quat tempQ2 = new Quat(entry.getValue()).mul(quat);
//						System.out.println(
//								"\norg:    " + entry.getValue() +
//								"\nrotAxis:" + tempQ1 +
//								"\nmul:    " + tempQ2);
//					}
					entry.getValue().rotateAxis(quat);
					if (newRotation.tans()) {
						entry.getInTan().rotateAxis(quat);
						entry.getOutTan().rotateAxis(quat);
					}
				}

			}
		}
	}

	private void rotRotationsQuatMul(Quat quat, AnimFlag<Quat> newRotation) {
		for (TreeMap<Integer, Entry<Quat>> entryMap : newRotation.getAnimMap().values()) {
			if (entryMap != null) {
				for (Entry<Quat> entry : entryMap.values()) {
//					if(entry.time == 0) {
//						Quat tempQ1 = new Quat(entry.getValue()).rotateAxis(quat);
//						Quat tempQ2 = new Quat(entry.getValue()).mul(quat);
//						System.out.println(
//								"\norg:    " + entry.getValue() +
//								"\nrotAxis:" + tempQ1 +
//								"\nmul:    " + tempQ2);
//					}
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
		if (rotTimelineAction != null) {
			rotTimelineAction.undo();
		}
		if (translTimelineAction != null) {
			translTimelineAction.undo();
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
		if (rotTimelineAction != null) {
			rotTimelineAction.redo();
		}
		if (translTimelineAction != null) {
			translTimelineAction.redo();
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
