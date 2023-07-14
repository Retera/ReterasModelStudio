package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.SetFlagEntryMapAction;
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

public class RotateNodeInAnimAction extends AbstractTransformAction {
	private final ModelStructureChangeListener changeListener;
	private final IdObject node;
	private final Sequence sequence;
//	private final Vec3 oldPivot;
//	private final Vec3 newPivot;
	private final Quat quat = new Quat();
	private double radians;
	private final Vec3 axis;
	private final Vec3 center;
	private final List<AddTimelineAction<?>> timelineActions = new ArrayList<>();
	private final List<SetFlagEntryMapAction<?>> timelineActions2 = new ArrayList<>();
	private final List<Vec3AnimFlag> newTranslations = new ArrayList<>();
	private final List<Vec3AnimFlag> newScalings = new ArrayList<>();
	private final List<QuatAnimFlag> newRotations = new ArrayList<>();
	private Vec3AnimFlag translationsFlag;
	private Vec3AnimFlag scalingsFlag;
	private QuatAnimFlag rotationsFlag;
	private TreeMap<Integer, Entry<Vec3>> transMapCopy;
	private TreeMap<Integer, Entry<Vec3>> scaleMapCopy;
	private TreeMap<Integer, Entry<Quat>> rotMapCopy;
	private TreeMap<Integer, Entry<Vec3>> orgTransMap;
	private TreeMap<Integer, Entry<Vec3>> orgScaleMap;
	private TreeMap<Integer, Entry<Quat>> orgRotMap;

	public RotateNodeInAnimAction(IdObject node, Sequence sequence,
	                              Vec3 axis, double radians, Vec3 center,
	                              ModelStructureChangeListener changeListener){
		this.changeListener = changeListener;
		this.radians = radians;
		this.axis = axis;
		this.center = center;
		this.node = node;
		this.sequence = sequence;
//		this.oldPivot = new Vec3(node.getPivotPoint());
//		this.newPivot = new Vec3(node.getPivotPoint()).rotate(center, quat);

		collectTimelines();
		createTimelineActions();
		rotate(radians);
	}

	private void collectTimelines() {
		AnimFlag<?> translation = node.find(MdlUtils.TOKEN_TRANSLATION);
		if (translation instanceof Vec3AnimFlag) {
//			newTranslations.add((Vec3AnimFlag)translation.deepCopy());
			translationsFlag = (Vec3AnimFlag)translation;
			transMapCopy = ((Vec3AnimFlag)translation).getSequenceEntryMapCopy(sequence);
			orgTransMap = ((Vec3AnimFlag)translation).getEntryMap(sequence);
		}
		AnimFlag<?> scaling = node.find(MdlUtils.TOKEN_SCALING);
		if (scaling instanceof Vec3AnimFlag) {
//			newScalings.add((Vec3AnimFlag)scaling.deepCopy());
			scalingsFlag = (Vec3AnimFlag)scaling;
			scaleMapCopy = ((Vec3AnimFlag)scaling).getSequenceEntryMapCopy(sequence);
			orgScaleMap = ((Vec3AnimFlag)scaling).getEntryMap(sequence);
		}
		AnimFlag<?> rotation = node.find(MdlUtils.TOKEN_ROTATION);
		if (rotation instanceof QuatAnimFlag) {
//			newRotations.add((QuatAnimFlag) rotation.deepCopy());
			rotationsFlag = (QuatAnimFlag)rotation;
			rotMapCopy = ((QuatAnimFlag)rotation).getSequenceEntryMapCopy(sequence);
			orgRotMap = ((QuatAnimFlag)rotation).getEntryMap(sequence);
		}
	}

	public RotateNodeInAnimAction doSetup() {
//		node.setPivotPoint(newPivot);
		for(UndoAction action : timelineActions2){
			action.redo();
		}
		return this;
	}

	private void createTimelineActions() {
		if(timelineActions2.isEmpty()){
			if(transMapCopy != null){
				timelineActions2.add(new SetFlagEntryMapAction<>(translationsFlag, sequence, transMapCopy, null));
			}
			if(rotMapCopy != null){
				timelineActions2.add(new SetFlagEntryMapAction<>(rotationsFlag, sequence, rotMapCopy, null));
			}
			if(scaleMapCopy != null){
				timelineActions2.add(new SetFlagEntryMapAction<>(scalingsFlag, sequence, scaleMapCopy, null));
			}

		}
//		if(timelineActions.isEmpty()){
//			for (Vec3AnimFlag newTranslation : newTranslations){
//				timelineActions.add(new AddTimelineAction<>(node, newTranslation));
//			}
//			for (Vec3AnimFlag newScaling : newScalings){
//				timelineActions.add(new AddTimelineAction<>(node, newScaling));
//			}
//			for (QuatAnimFlag newRotation : newRotations){
//				timelineActions.add(new AddTimelineAction<>(node, newRotation));
//			}
//		}
	}

	public RotateNodeInAnimAction setRotation(double radians) {
		double rotDiff = radians - this.radians;
		this.radians = radians;
		rotate(rotDiff);
//		node.setPivotPoint(newPivot);
		return this;
	}

	public RotateNodeInAnimAction updateRotation(double radians){
		this.radians += radians;
		rotate(radians);
//		node.setPivotPoint(newPivot);
		return this;
	}

	private void rotate(double radians) {
		quat.setFromAxisAngle(axis, (float) -radians);
//		newPivot.rotate(center, quat);
		updateTimelines(radians);
	}

	public RotateNodeInAnimAction setRotation(Quat rot) {
//		double rotDiff = radians - this.radians;
//		this.radians = radians;
		updateTimelines(rot);
//		node.setPivotPoint(newPivot);
		return this;
	}

	private void updateTimelines(double radians) {
		quat.setFromAxisAngle(axis, (float) -radians);

		if (transMapCopy != null) {
			for (Entry<Vec3> entry : transMapCopy.values()) {
				entry.getValue().rotate(Vec3.ZERO, quat);
				if (entry.outTan != null) {
					entry.getInTan().rotate(Vec3.ZERO, quat);
					entry.getOutTan().rotate(Vec3.ZERO, quat);
				}
			}
		}
		Vec3 tempScaleMul = new Vec3(Vec3.ONE).rotate(Vec3.ZERO, quat);
		if (scaleMapCopy != null) {
			for (Entry<Vec3> entry : scaleMapCopy.values()) {
				entry.getValue().rotate(Vec3.ZERO, quat).multiply(tempScaleMul);
				if (entry.outTan != null) {
					entry.getInTan().rotate(Vec3.ZERO, quat).multiply(tempScaleMul);
					entry.getOutTan().rotate(Vec3.ZERO, quat).multiply(tempScaleMul);
				}
			}
		}
		Vec3 tempAxis = new Vec3();
		if (rotMapCopy != null) {
			for (Entry<Quat> entry : rotMapCopy.values()) {
				tempAxis.setAsAxis(entry.getValue());
				float axisAngle = entry.getValue().getAxisAngle();
				tempAxis.rotate(Vec3.ZERO, quat);
				entry.getValue().setFromAxisAngle(tempAxis, axisAngle);
				if (entry.outTan != null) {
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

	private void updateTimelines(Quat quat) {
		if (transMapCopy != null) {
			for (Entry<Vec3> entry : transMapCopy.values()) {
				entry.cloneFrom(orgTransMap.get(entry.time));
				entry.getValue().rotate(Vec3.ZERO, quat);
				if (entry.outTan != null) {
					entry.getInTan().rotate(Vec3.ZERO, quat);
					entry.getOutTan().rotate(Vec3.ZERO, quat);
				}
			}
		}
		Vec3 tempScaleMul = new Vec3(Vec3.ONE).rotate(Vec3.ZERO, quat);
		if (scaleMapCopy != null) {
			for (Entry<Vec3> entry : scaleMapCopy.values()) {
				entry.cloneFrom(orgScaleMap.get(entry.time));
				entry.getValue().rotate(Vec3.ZERO, quat).multiply(tempScaleMul);
				if (entry.outTan != null) {
					entry.getInTan().rotate(Vec3.ZERO, quat).multiply(tempScaleMul);
					entry.getOutTan().rotate(Vec3.ZERO, quat).multiply(tempScaleMul);
				}
			}
		}
//		Vec3 tempAxis = new Vec3();
		if (rotMapCopy != null) {
			for (Entry<Quat> entry : rotMapCopy.values()) {
				entry.cloneFrom(orgRotMap.get(entry.time));
				entry.getValue().mul(quat);

				if (entry.outTan != null) {
					entry.getInTan().mul(quat);

					entry.getOutTan().mul(quat);
				}
//				entry.cloneFrom(orgRotMap.get(entry.time));
//				tempAxis.setAsAxis(entry.getValue());
//				float axisAngle = entry.getValue().getAxisAngle();
//				tempAxis.rotate(Vec3.ZERO, quat);
//				entry.getValue().setFromAxisAngle(tempAxis, axisAngle);
//				if (entry.outTan != null) {
//					tempAxis.setAsAxis(entry.getInTan());
//					axisAngle = entry.getInTan().getAxisAngle();
//					tempAxis.rotate(Vec3.ZERO, quat);
//					entry.getInTan().setFromAxisAngle(tempAxis, axisAngle);
//
//					tempAxis.setAsAxis(entry.getOutTan());
//					axisAngle = entry.getOutTan().getAxisAngle();
//					tempAxis.rotate(Vec3.ZERO, quat);
//					entry.getOutTan().setFromAxisAngle(tempAxis, axisAngle);
//				}
			}

		}
	}

	@Override
	public RotateNodeInAnimAction undo() {
		for(UndoAction action : timelineActions2){
			action.undo();
		}
//		node.setPivotPoint(oldPivot);
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public RotateNodeInAnimAction redo() {
//		node.setPivotPoint(newPivot);
		for(UndoAction action : timelineActions2){
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
