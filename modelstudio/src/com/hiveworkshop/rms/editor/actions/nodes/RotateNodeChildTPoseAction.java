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

public class RotateNodeChildTPoseAction extends AbstractTransformAction {
	private final ModelStructureChangeListener changeListener;
	private final IdObject node;
	private final Vec3 oldPivot;
	private final Vec3 newPivot;
	private final Quat quat = new Quat();
	private final Vec3 axis;
	private double radians;
	private final Vec3 center;
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();
	private final List<AddTimelineAction<?>> timelineActions = new ArrayList<>();
	private final List<Vec3AnimFlag> newTranslations = new ArrayList<>();
//	private final List<Vec3AnimFlag> newScalings = new ArrayList<>();
	private final List<QuatAnimFlag> newRotations = new ArrayList<>();

	public RotateNodeChildTPoseAction(IdObject node,
	                                  Vec3 center, Quat quat,
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
		this.newPivot = new Vec3(node.getPivotPoint());

		collectTimelines();
		createTimelineActions();
		rotate(quat);
	}
	public RotateNodeChildTPoseAction(IdObject node,
	                                  Vec3 axis, double radians, Vec3 center,
	                                  Mat4 rotMat,
	                                  ModelStructureChangeListener changeListener){
		this.changeListener = changeListener;
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
		this.axis = axis;
		this.radians = radians;
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
//		AnimFlag<?> scaling = node.find(MdlUtils.TOKEN_SCALING);
//		if (scaling instanceof Vec3AnimFlag) {
//			newScalings.add((Vec3AnimFlag)scaling.deepCopy());
//		}
		AnimFlag<?> rotation = node.find(MdlUtils.TOKEN_ROTATION);
		if (rotation instanceof QuatAnimFlag) {
			newRotations.add((QuatAnimFlag) rotation.deepCopy());
		}
	}

	public RotateNodeChildTPoseAction doSetup() {
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
//			for (Vec3AnimFlag newScaling : newScalings){
//				timelineActions.add(new AddTimelineAction<>(node, newScaling));
//			}
			for (QuatAnimFlag newRotation : newRotations){
				timelineActions.add(new AddTimelineAction<>(node, newRotation));
			}
		}
	}

	public RotateNodeChildTPoseAction updateRotation(double radians){
		this.radians += radians;
		rotate(radians);
		node.setPivotPoint(newPivot);
		return this;
	}
	public RotateNodeChildTPoseAction setRotation(double radians){
		double rotDiff = radians - this.radians;
		this.radians = radians;
		rotate(rotDiff);
		node.setPivotPoint(newPivot);
		return this;
	}

	private void rotate(double radians) {
		System.out.println("rotating " + node.getName() + " \t" + ((float)Math.toDegrees(radians)) + " deg around " + axis);
		quat.setFromAxisAngle(axis, (float) -radians);
		newPivot.rotate(center, quat);
		updateTimelines(quat);
	}

	private void updateTimelines(Quat quat) {
		for (Vec3AnimFlag newTranslation : newTranslations){
			rotTranslation(quat, newTranslation);
		}

//		Vec3 tempScaleMul = new Vec3(Vec3.ONE).rotate(Vec3.ZERO, quat);
//		for (Vec3AnimFlag newScaling : newScalings){
//			rotScaling(quat, tempScaleMul, newScaling);
//		}
		Vec3 tempAxis = new Vec3();
		for (QuatAnimFlag newRotation : newRotations){
			rotRotation(quat, tempAxis, newRotation);
		}
	}

	private void rotate(Quat quat) {
		System.out.println("rotating " + node.getName() + " " + Math.toDegrees(quat.getAxisAngle()) + "deg around " + quat.getAxis());
		newPivot.rotate(center, quat);
		updateTimelines(quat);
	}

	private void rotTranslation(Quat quat, Vec3AnimFlag newTranslation) {
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

	private void rotScaling(Quat quat, Vec3 tempScaleMul, Vec3AnimFlag newScaling) {
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

	private void rotRotation(Quat quat, Vec3 tempAxis, QuatAnimFlag newRotation) {
		for (TreeMap<Integer, Entry<Quat>> entryMap : newRotation.getAnimMap().values()) {
			if (entryMap != null) {
				System.out.println("\tadjusting rot! : " + quat);
				for (Entry<Quat> entry : entryMap.values()) {
//						System.out.println("\t\ttempAxis: " + tempAxis + " (entry: " + entry.getValue() + ")");
					tempAxis.setAsAxis(entry.getValue());
					float axisAngle = entry.getValue().getAxisAngle();
					tempAxis.rotate(Vec3.ZERO, quat);
					entry.getValue().setFromAxisAngle(tempAxis, axisAngle);
//						System.out.println("\t\tnewAxis:  " + tempAxis + ", angle: " + axisAngle  + " (entry: " + entry.getValue() + ")");
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

	@Override
	public RotateNodeChildTPoseAction undo() {
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
	public RotateNodeChildTPoseAction redo() {
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
