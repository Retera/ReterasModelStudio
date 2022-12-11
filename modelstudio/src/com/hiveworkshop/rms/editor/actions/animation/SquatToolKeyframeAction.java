package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class SquatToolKeyframeAction extends AbstractTransformAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final int trackTime;
	private final HashMap<IdObject, Quat> nodeToLocalRotation;
	private final Vec3 center;
	private final Vec3 axis;
	private final Sequence anim;
	private final RenderModel editorRenderModel;
	private final Collection<IdObject> nodeSelection;
	private final List<IdObject> squatObjects;
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();
	private double radians;


	public SquatToolKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
	                               Collection<IdObject> nodeSelection,
	                               RenderModel editorRenderModel,
	                               Vec3 center,
	                               Vec3 axis, Mat4 rotMat) {
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.editorRenderModel = editorRenderModel;
		this.trackTime = editorRenderModel.getTimeEnvironment().getEnvTrackTime();
		this.anim = editorRenderModel.getTimeEnvironment().getCurrentSequence();
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();

		this.nodeSelection = new HashSet<>(nodeSelection);

		this.axis = axis;
		this.center = new Vec3(center);
		nodeToLocalRotation = new HashMap<>();

		for (IdObject idObject : nodeSelection) {
			nodeToLocalRotation.put(idObject, new Quat());
		}

		Set<IdObject> childSet = new HashSet<>();
		for (IdObject idObject : nodeSelection) {
			childSet.addAll(idObject.getChildrenNodes());
		}

		squatObjects = new ArrayList<>();
		for (IdObject idObject : childSet) {
			if (nodeToLocalRotation.containsKey(idObject.getParent()) && idObject instanceof Bone
					&& idObject.getParent() instanceof Bone && !(idObject.getParent() instanceof Helper)) {
				squatObjects.add(idObject);
			}
		}

//		// I'm not sure if multiple selected nodes should be locked as one section (as above)
//		// or behave like normal rotate with the child at the end of the chain keeping
//		// its original orientation (as below)
//		squatObjects = new ArrayList<>();
//		for (IdObject idObject : childSet) {
//			if (!nodeToLocalRotation.containsKey(idObject) && nodeToLocalRotation.containsKey(idObject.getParent())
//					&& (idObject instanceof Bone && idObject.getParent() instanceof Bone
//					&& idObject instanceof Helper == idObject.getParent() instanceof Helper)) {
//				IdObject parent = idObject.getParent();
//				while(parent != null && nodeToLocalRotation.containsKey(parent)){
//					squatObjects.add(idObject);
//					parent = parent.getParent();
//				}
//			}
//		}

		for (IdObject idObject : squatObjects) {
			nodeToLocalRotation.put(idObject, new Quat());
		}
	}

	@Override
	public SquatToolKeyframeAction undo() {
		Quat localRotation = new Quat();
		for (IdObject node : nodeToLocalRotation.keySet()) {
			localRotation.set(nodeToLocalRotation.get(node)).invertRotation();
			updateLocalRotationKeyframe(node, localRotation);
		}
		if(addingTimelinesOrKeyframesAction != null){
			addingTimelinesOrKeyframesAction.undo();
		}
		return this;
	}

	@Override
	public SquatToolKeyframeAction redo() {
		if(addingTimelinesOrKeyframesAction != null){
			addingTimelinesOrKeyframesAction.redo();
		}
		for (IdObject node : nodeToLocalRotation.keySet()) {
			Quat localRotation = nodeToLocalRotation.get(node);
			updateLocalRotationKeyframe(node, localRotation);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "edit rotation w/ squat";
	}

	public SquatToolKeyframeAction doSetup(){
		if(addingTimelinesOrKeyframesAction != null){
			addingTimelinesOrKeyframesAction.redo();
		}
		return this;
	}

	@Override
	public SquatToolKeyframeAction updateRotation(double radians) {
		// Note to future author: the reason for saved local rotation is that
		// the input rotation took place in world space and got converted into local space
		this.radians += radians;
		for (IdObject node : nodeSelection) {
			if (nodeToLocalRotation.get(node) != null) {
				Quat rotation = getRotation(node, axis, (float) radians);
				nodeToLocalRotation.get(node).mul(rotation);
				updateLocalRotationKeyframe(node, rotation);
			}
		}
		for (IdObject node : squatObjects) {
			// cancel the parent rotation
			if (nodeToLocalRotation.get(node) != null) {
				Quat rotation = getRotation(node, axis, (float) radians).invertRotation();
				nodeToLocalRotation.get(node).mul(rotation);
				updateLocalRotationKeyframe(node, rotation);
			}
		}

		return this;
	}

	@Override
	public SquatToolKeyframeAction setRotation(double radians) {
		// Note to future author: the reason for saved local rotation is that
		// the input rotation took place in world space and got converted into local space
		double rotDiff = radians - this.radians;
		this.radians = radians;
		for (IdObject node : nodeSelection) {
			if (nodeToLocalRotation.get(node) != null) {
				Quat rotation = getRotation(node, axis, (float) rotDiff);
				nodeToLocalRotation.get(node).mul(rotation);
				updateLocalRotationKeyframe(node, rotation);
			}
		}
		for (IdObject node : squatObjects) {
			// cancel the parent rotation
			if (nodeToLocalRotation.get(node) != null) {
				Quat rotation = getRotation(node, axis, (float) rotDiff).invertRotation();
				nodeToLocalRotation.get(node).mul(rotation);
				updateLocalRotationKeyframe(node, rotation);
			}
		}

		return this;
	}

	public void updateLocalRotationKeyframe(AnimatedNode animatedNode, Quat localRotation) {
		QuatAnimFlag animFlag = (QuatAnimFlag) animatedNode.find(MdlUtils.TOKEN_ROTATION);
		if (animFlag == null || anim instanceof GlobalSeq && animFlag.getGlobalSeq() != anim) {
			return;
		}

		if (animFlag.hasEntryAt(anim, trackTime)) {
			Entry<Quat> entry = animFlag.getEntryAt(anim, trackTime);
			entry.getValue().mul(localRotation);

			if (animFlag.tans()) {
				entry.getInTan().mul(localRotation);
				entry.getOutTan().mul(localRotation);
			}
		}
	}

	Quat tempQuat = new Quat();
	Vec3 tempAxis = new Vec3();
	Vec3 tempAxis2 = new Vec3();
	Mat4 tempMat = new Mat4();

	private Quat getRotation(AnimatedNode idObject, Vec3 axis, float radians) {
		RenderNode<?> renderNode = editorRenderModel.getRenderNode(idObject);
		tempAxis.set(axis).transform(invRotMat, 1, true);
		if(renderNode != null){
			tempMat.set(renderNode.getWorldMatrix()).invert();

			tempAxis.add(renderNode.getPivot()).transform(tempMat, 1, true);
			tempAxis2.set(renderNode.getPivot()).transform(tempMat, 1, true);
			return tempQuat.setFromAxisAngle(tempAxis.sub(tempAxis2).normalize(), -radians).normalize();
		}
		return tempQuat.setIdentity();
	}
}
