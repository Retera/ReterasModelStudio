package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
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

public class RotateNodeTPoseAction2 extends AbstractTransformAction {
	private final ModelStructureChangeListener changeListener;
	private final IdObject node;
	private final Vec3 oldPivot;
	private final Vec3 newPivot;
	private double radians;
	private final Quat quat = new Quat();
	private final Vec3 axis;
	private final Vec3 center;
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();
	private final List<AddTimelineAction<?>> timelineActions = new ArrayList<>();
	private final List<Vec3AnimFlag> newTranslations = new ArrayList<>();
//	private final List<Vec3AnimFlag> newScalings = new ArrayList<>();
	private final List<QuatAnimFlag> newRotations = new ArrayList<>();
	private final List<RotateNodeChildTPoseAction> rotChildActions = new ArrayList<>();

	public RotateNodeTPoseAction2(IdObject node,
	                              Vec3 axis, double radians, Vec3 center,
	                              Mat4 rotMat,
	                              ModelStructureChangeListener changeListener){
		this.changeListener = changeListener;
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
		this.radians = radians;
		this.axis = axis;
		this.center = center;
		this.node = node;
		this.oldPivot = new Vec3(node.getPivotPoint());
		quat.setFromAxisAngle(axis, (float) radians);
		this.newPivot = new Vec3(node.getPivotPoint()).rotate(center, quat);

		for(IdObject child : node.getChildrenNodes()){
			collectRots(child, node.getPivotPoint(), axis, -radians);
		}
		collectTimelines();
		createTimelineActions();
		quat.setIdentity().mulInverse(new Quat(axis, (float) radians)).normalize();
		rotate(quat.getAxisAngle());
	}

	public RotateNodeTPoseAction2(IdObject node,
	                              Quat quat, Vec3 center,
	                              Mat4 rotMat,
	                              ModelStructureChangeListener changeListener){
		this.changeListener = changeListener;
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
		this.axis = quat.getAxis();
		this.radians = quat.getAxisAngle();
		this.center = center;
		this.node = node;
		this.oldPivot = new Vec3(node.getPivotPoint());
		this.newPivot = new Vec3(node.getPivotPoint()).rotate(center, quat);

		for(IdObject child : node.getChildrenNodes()){
			collectRots(child, node.getPivotPoint(), axis, -quat.getAxisAngle());
		}
		collectTimelines();
		createTimelineActions();
		this.quat.mulInverse(quat).normalize();
		rotate(quat.getAxisAngle());
	}

	private void collectRots(IdObject node, Vec3 pivot, Vec3 axis, double radians){
		System.out.println("\t collecting node: " + node.getName());
		rotChildActions.add(new RotateNodeChildTPoseAction(node, axis, radians, pivot, rotMat, null));
		for(IdObject child : node.getChildrenNodes()){
			collectRots(child, pivot, axis, radians);
		}
	}

	private void collectTimelines() {
		AnimFlag<?> translation = node.find(MdlUtils.TOKEN_TRANSLATION);
		if (translation instanceof Vec3AnimFlag) {
			newTranslations.add((Vec3AnimFlag)translation.deepCopy());
		}
//		AnimFlag<?> scaling = node.find(MdlUtils.TOKEN_SCALING);
//		if (scaling instanceof Vec3AnimFlag) {
//			newScalings.add((Vec3AnimFlag)scaling.deepCopy());
//		}
		AnimFlag<?> rotation = node.find(MdlUtils.TOKEN_ROTATION);
		if (rotation instanceof QuatAnimFlag) {
			newRotations.add((QuatAnimFlag) rotation.deepCopy());
		}
	}

	public RotateNodeTPoseAction2 doSetup() {
		node.setPivotPoint(newPivot);
		for(RotateNodeChildTPoseAction action : rotChildActions){
			action.redo();
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
//			for (Vec3AnimFlag newScaling : newScalings){
//				timelineActions.add(new AddTimelineAction<>(node, newScaling));
//			}
			for (QuatAnimFlag newRotation : newRotations){
				timelineActions.add(new AddTimelineAction<>(node, newRotation));
			}
		}
	}

	public RotateNodeTPoseAction2 updateRotation(double radians){
		this.radians += radians;
		for(RotateNodeChildTPoseAction action : rotChildActions){
			action.updateRotation(radians);
		}
		System.out.println("rotating: " + radians);
		rotate(-radians);
		node.setPivotPoint(newPivot);
		return this;
	}
	public RotateNodeTPoseAction2 setRotation(double radians){
		double rotDiff = radians - this.radians;
		this.radians = radians;
		for(RotateNodeChildTPoseAction action : rotChildActions){
			action.setRotation(radians);
		}
		System.out.println("rotating: " + radians);
		rotate(-rotDiff);
		node.setPivotPoint(newPivot);
		return this;
	}

	private void rotate(double radians) {
		quat.setFromAxisAngle(axis, (float) radians);
		newPivot.rotate(center, quat);
		updateTimelines(radians);
	}

	private void updateTimelines(double radians) {
		quat.setFromAxisAngle(axis, (float) -radians);
//		for (Vec3AnimFlag newTranslation : newTranslations){
//			rotTranslations(quat, newTranslation);
//		}

//		Vec3 tempScaleMul = new Vec3(Vec3.ONE).rotate(Vec3.ZERO, quat);
//		for (Vec3AnimFlag newScaling : newScalings){
//			rotScalings(quat, tempScaleMul, newScaling);
//		}
		for (QuatAnimFlag newRotation : newRotations){
			rotRotations(quat, newRotation);
		}
	}

	private void rotate(Quat quat) {
		System.out.println("rotating " + node.getName() + " " + Math.toDegrees(quat.getAxisAngle()) + "deg around " + quat.getAxis());
		newPivot.rotate(center, quat);
		updateTimelines(quat);
	}

	private void updateTimelines(Quat quat) {
		for (Vec3AnimFlag newTranslation : newTranslations){
			rotTranslations(quat, newTranslation);
		}
//		Vec3 tempScaleMul = new Vec3(Vec3.ONE).rotate(Vec3.ZERO, quat);
//		for (Vec3AnimFlag newScaling : newScalings){
//			rotScalings(quat, tempScaleMul, newScaling);
//		}
		for (QuatAnimFlag newRotation : newRotations){
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
				System.out.println("\tadjusting rot! : " + quat);
//				Vec3 tempAxis = new Vec3();
				for (Entry<Quat> entry : entryMap.values()) {
//					System.out.println("\t\ttempAxis: " + tempAxis + " (entry: " + entry.getValue() + ")");
					entry.getValue().mul(quat);
//					System.out.println("\t\tnewAxis:  " + tempAxis + ", angle: " + quat.getAxisAngle()  + " (entry: " + entry.getValue() + ")");
					if (newRotation.tans()) {
						entry.getInTan().mul(quat);
						entry.getOutTan().mul(quat);
					}
				}

			}
		}
	}

	@Override
	public RotateNodeTPoseAction2 undo() {
		for(UndoAction action : timelineActions){
			action.undo();
		}
		for(RotateNodeChildTPoseAction action : rotChildActions){
			action.undo();
		}
		node.setPivotPoint(oldPivot);
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public RotateNodeTPoseAction2 redo() {
		node.setPivotPoint(newPivot);
		for(RotateNodeChildTPoseAction action : rotChildActions){
			action.redo();
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
}
