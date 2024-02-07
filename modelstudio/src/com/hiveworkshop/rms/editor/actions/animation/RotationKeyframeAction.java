package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.*;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.util.Collection;
import java.util.HashMap;

public class RotationKeyframeAction extends AbstractTransformAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final int trackTime;
	private final HashMap<AnimatedNode, Quat> nodeToLocalRotation;
	private final HashMap<AnimatedNode, Entry<?>> nodeToOrgRotation;
	private final Vec3 center;
	private final Vec3 axis;
	private final Sequence anim;
	private final RenderModel editorRenderModel;
	private final float PI_x2 = (float) (2*Math.PI);
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();
	private double radians;
	private final boolean worldSpace;

	// ToDo should maybe consider center and make a rotation around a point; ie do a combined rotation and translation
	//  maybe also have an option for individual origins

	public RotationKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
	                              Collection<IdObject> nodeSelection,
	                              Collection<CameraNode> camSelection,
	                              RenderModel editorRenderModel,
	                              Vec3 center,
	                              Vec3 axis, double radians, Mat4 rotMat) {
		this(addingTimelinesOrKeyframesAction, nodeSelection, camSelection, editorRenderModel, center, axis, radians, true, rotMat);
	}

	public RotationKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
	                              Collection<IdObject> nodeSelection,
	                              Collection<CameraNode> camSelection,
	                              RenderModel editorRenderModel,
	                              Vec3 center,
	                              Vec3 axis, double radians, boolean worldSpace, Mat4 rotMat) {
		this.editorRenderModel = editorRenderModel;
		this.trackTime = editorRenderModel.getTimeEnvironment().getEnvTrackTime();
		this.anim = editorRenderModel.getTimeEnvironment().getCurrentSequence();
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
		this.radians = radians;
		this.worldSpace = worldSpace;
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;

		nodeToOrgRotation = new HashMap<>();
		nodeToLocalRotation = new HashMap<>();
		for (IdObject node : nodeSelection) {
			AnimFlag<?> rotationFlag = node.find(MdlUtils.TOKEN_ROTATION);
			if (rotationFlag != null && rotationFlag.hasEntryAt(anim, trackTime)) {
				nodeToOrgRotation.put(node, rotationFlag.getEntryAt(anim, trackTime).deepCopy());
			}
			nodeToLocalRotation.put(node, new Quat());
		}
		for (CameraNode node : camSelection) {
			AnimFlag<?> rotationFlag = node.find(MdlUtils.TOKEN_ROTATION);
			if (rotationFlag != null && rotationFlag.hasEntryAt(anim, trackTime)) {
				nodeToOrgRotation.put(node, rotationFlag.getEntryAt(anim, trackTime).deepCopy());
			}
			nodeToLocalRotation.put(node, new Quat());
		}
		this.center = new Vec3(center);
		this.axis = axis;
		for (AnimatedNode node : nodeToLocalRotation.keySet()) {
			if (nodeToLocalRotation.get(node) != null) {
				Quat rotation = getRotation(node, axis, (float) radians);
				nodeToLocalRotation.get(node).mul(rotation);
			}
		}
	}


	@Override
	public RotationKeyframeAction undo() {
		for (AnimatedNode node : nodeToOrgRotation.keySet()) {
			Entry<?> entry = nodeToOrgRotation.get(node);
			AnimFlag<?> animFlag = node.find(MdlUtils.TOKEN_ROTATION);
			if (entry != null && animFlag != null) {
				animFlag.setOrAddEntryT(entry.getTime(), entry.deepCopy(), anim);
			}
		}

		if (addingTimelinesOrKeyframesAction != null) {
			addingTimelinesOrKeyframesAction.undo();
		}
		return this;
	}

	@Override
	public RotationKeyframeAction redo() {
		if (addingTimelinesOrKeyframesAction != null) {
			addingTimelinesOrKeyframesAction.redo();
		}
		for (AnimatedNode node : nodeToLocalRotation.keySet()) {
			Quat localRotation = nodeToLocalRotation.get(node);
			updateLocalRotationKeyframe(node, localRotation);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Edit Rotation";
	}

	public RotationKeyframeAction doSetup() {
		if (addingTimelinesOrKeyframesAction != null) {
			addingTimelinesOrKeyframesAction.redo();
		}
		return this;
	}

	@Override
	public RotationKeyframeAction updateRotation(double radians) {
		// Note to future author: the reason for saved local rotation is that
		// the input rotation took place in world space and got converted into local space
		this.radians += radians;
		for (AnimatedNode node : nodeToLocalRotation.keySet()) {
			if (nodeToLocalRotation.get(node) != null) {
				Quat rotation = getRotation(node, axis, (float) radians);
				nodeToLocalRotation.get(node).mul(rotation);
				updateLocalRotationKeyframe(node, rotation);
			}
		}
		return this;
	}
	@Override
	public RotationKeyframeAction setRotation(double radians) {
		double rotDiff = radians - this.radians;
		this.radians = radians;
		for (AnimatedNode node : nodeToLocalRotation.keySet()) {
			if (nodeToLocalRotation.get(node) != null) {
				Quat rotation = getRotation(node, axis, (float) rotDiff);
				nodeToLocalRotation.get(node).mul(rotation);
				updateLocalRotationKeyframe(node, rotation);
			}
		}
		return this;
	}

	private void updateLocalRotationKeyframe(AnimatedNode animatedNode, Quat localRotation) {
		AnimFlag<?> animFlag = animatedNode.find(MdlUtils.TOKEN_ROTATION);
		if (animFlag instanceof QuatAnimFlag) {
			updateQuatRotation(localRotation, (QuatAnimFlag) animFlag);
		} else if (animFlag instanceof FloatAnimFlag) {
			updateFloatRotation(localRotation, (FloatAnimFlag) animFlag, animatedNode);
		} else if (animFlag instanceof IntAnimFlag) {
			updateIntRotation(localRotation, (IntAnimFlag) animFlag, animatedNode);
		}
	}

	private void updateQuatRotation(Quat localRotation, QuatAnimFlag animFlag) {
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


	Vec4 temp = new Vec4();
	private void updateFloatRotation(Quat localRotation, FloatAnimFlag animFlag, AnimatedNode animatedNode) {
		if (animFlag == null || anim instanceof GlobalSeq && animFlag.getGlobalSeq() != anim) {
			return;
		}

		if (animatedNode instanceof CameraNode) {
			if (animFlag.hasEntryAt(anim, trackTime)) {
				Vec3 cameraLookAxis = new Vec3();
				Vec3 cameraLookAxis2 = new Vec3();
				Vec3 pivot = editorRenderModel.getRenderNode((CameraNode) animatedNode).getPivot();
				Vec3 target = editorRenderModel.getRenderNode((CameraNode) animatedNode).getTarget();
				cameraLookAxis.set(target).sub(pivot).normalize();
				cameraLookAxis2.set(target).sub(pivot).normalize();

				cameraLookAxis2.transform(localRotation);

				temp.setAsAxisWithAngle(localRotation);
				float v = Math.copySign(temp.w, temp.y);


				Entry<Float> entry = animFlag.getEntryAt(anim, trackTime);
				entry.setValue((entry.getValue() + v)%PI_x2);

				totRot2 += v;

				if (animFlag.tans()) {
					entry.setInTan((entry.getInTan() + v)%PI_x2);
					entry.setOutTan((entry.getOutTan() + v)%PI_x2);
				}
			}
		}
	}
	float totRot2 = 0;
	private void updateIntRotation(Quat localRotation, IntAnimFlag animFlag, AnimatedNode animatedNode) {
		if (animFlag == null || anim instanceof GlobalSeq && animFlag.getGlobalSeq() != anim) {
			return;
		}

		if (animatedNode instanceof CameraNode) {
			if (animFlag.hasEntryAt(anim, trackTime)) {
				Vec3 cameraLookAxis = new Vec3();
				Vec3 cameraLookAxis2 = new Vec3();
				Vec3 pivot = editorRenderModel.getRenderNode((CameraNode) animatedNode).getPivot();
				Vec3 target = editorRenderModel.getRenderNode((CameraNode) animatedNode).getTarget();
				cameraLookAxis.set(target).sub(pivot).normalize();
				cameraLookAxis2.set(target).sub(pivot).normalize();
				cameraLookAxis2.transform(localRotation);

				int v = (int) cameraLookAxis2.radAngleTo2(cameraLookAxis);

				Entry<Integer> entry = animFlag.getEntryAt(anim, trackTime);
				entry.setValue(entry.getValue() + v);
				totRot2 += v;

				if (animFlag.tans()) {
					entry.setInTan(entry.getInTan() + v);
					entry.setOutTan(entry.getOutTan() + v);
				}
			}
		}
	}

//	float totRot = 0;
	Quat tempQuat = new Quat();
	Vec3 tempAxis = new Vec3();
	Vec3 tempAxis2 = new Vec3();
	Mat4 tempMat = new Mat4();
	private Quat getRotation(AnimatedNode idObject, Vec3 axis, float radians) {
		RenderNode<?> renderNode = editorRenderModel.getRenderNode(idObject);
		tempAxis.set(axis).transform(invRotMat, 1, true);
		if (renderNode != null) {
			if (worldSpace) {
				tempMat.set(renderNode.getWorldMatrix()).invert();

				tempAxis.add(renderNode.getPivot()).transform(tempMat, 1, true);
				tempAxis2.set(renderNode.getPivot()).transform(tempMat, 1, true);
				tempAxis.sub(tempAxis2);
			}

			return tempQuat.setFromAxisAngle(tempAxis.normalize(), -radians).normalize();
		}
		return tempQuat.setIdentity();
	}
}
