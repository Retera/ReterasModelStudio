package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.util.Collection;
import java.util.HashMap;

public class RotationKeyframeAction2 implements GenericRotateAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final int trackTime;
	private final HashMap<IdObject, Quat> nodeToLocalRotation;
	private final Vec3 center;
	private final Vec3 axis;
	private final GlobalSeq trackGlobalSeq;
	private final RenderModel editorRenderModel;

	public RotationKeyframeAction2(UndoAction addingTimelinesOrKeyframesAction,
	                               Collection<IdObject> nodeSelection,
	                               RenderModel editorRenderModel,
	                               Vec3 center,
	                               Vec3 axis) {
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.editorRenderModel = editorRenderModel;
		this.trackTime = editorRenderModel.getTimeEnvironment().getEnvTrackTime();
		this.trackGlobalSeq = editorRenderModel.getTimeEnvironment().getGlobalSeq();
		this.axis = axis;
		nodeToLocalRotation = new HashMap<>();
		for (IdObject node : nodeSelection) {
			nodeToLocalRotation.put(node, new Quat());
		}
		this.center = new Vec3(center);
	}

	@Override
	public UndoAction undo() {
		for (IdObject node : nodeToLocalRotation.keySet()) {
			Quat localRotation = nodeToLocalRotation.get(node).invertRotation();
//			updateLocalRotationKeyframeInverse(node, trackTime, trackGlobalSeq, localRotation);
			updateLocalRotationKeyframe(node, trackTime, trackGlobalSeq, localRotation);
			localRotation.invertRotation();
		}
		addingTimelinesOrKeyframesAction.undo();
		return this;
	}

	@Override
	public UndoAction redo() {
		addingTimelinesOrKeyframesAction.redo();
		for (IdObject node : nodeToLocalRotation.keySet()) {
			Quat localRotation = nodeToLocalRotation.get(node);
			updateLocalRotationKeyframe(node, trackTime, trackGlobalSeq, localRotation);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "edit rotation";
	}

	@Override
	public GenericRotateAction updateRotation(double radians) {
		for (IdObject idObject : nodeToLocalRotation.keySet()) {
			updateRotationKeyframe(idObject, editorRenderModel, center, radians, axis, nodeToLocalRotation.get(idObject));
		}
		return this;
	}

	public void updateLocalRotationKeyframe(AnimatedNode animatedNode, int trackTime, GlobalSeq trackGlobalSeq, Quat localRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		QuatAnimFlag rotationTimeline = (QuatAnimFlag) animatedNode.find(MdlUtils.TOKEN_ROTATION, trackGlobalSeq);
		if (rotationTimeline == null) {
			return;
		}
		if (rotationTimeline.hasEntryAt(trackTime)) {
			Entry<Quat> entry = rotationTimeline.getEntryAt(trackTime);
			entry.getValue().mul(localRotation);
			if (rotationTimeline.tans()) {
				entry.getInTan().mul(localRotation);
				entry.getOutTan().mul(localRotation);
			}
		}
	}

	public void updateLocalRotationKeyframeInverse(AnimatedNode animatedNode, int trackTime, GlobalSeq trackGlobalSeq, Quat localRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		QuatAnimFlag rotationTimeline = (QuatAnimFlag) animatedNode.find(MdlUtils.TOKEN_ROTATION, trackGlobalSeq);
		if (rotationTimeline == null) {
			return;
		}
		if (rotationTimeline.hasEntryAt(trackTime)) {
			Entry<Quat> entry = rotationTimeline.getEntryAt(trackTime);
			entry.getValue().mulLeft(localRotation.invertRotation());
			if (rotationTimeline.tans()) {
				entry.getInTan().mulLeft(localRotation.invertRotation());
				entry.getOutTan().mulLeft(localRotation.invertRotation());
			}
		}
	}

	public void updateRotationKeyframe(AnimatedNode animatedNode, RenderModel renderModel, Vec3 center,
	                                   double radians, Vec3 axis, Quat savedLocalRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be  constructed from
		//  a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getTimeEnvironment();
		QuatAnimFlag rotationTimeline = (QuatAnimFlag) animatedNode.find(MdlUtils.TOKEN_ROTATION, timeEnvironmentImpl.getGlobalSeq());
		if (rotationTimeline == null) {
			return;
		}
		int trackTime = renderModel.getTimeEnvironment().getEnvTrackTime();

		AnimatedNode parent = null;// = getParent();
		if (animatedNode instanceof IdObject) {
			parent = ((IdObject) animatedNode).getParent();
		}

		Vec4 rotationAxis = new Vec4(0, 0, 0, 1);

		if (parent != null) {
			RenderNode parentRenderNode = renderModel.getRenderNode(parent);

			rotationAxis.transform(parentRenderNode.getWorldMatrix());
			rotationAxis.add(axis);
			rotationAxis.transform(Mat4.getInverted(parentRenderNode.getWorldMatrix()));
		} else {
			rotationAxis.add(axis);
		}
		rotationAxis.w = (float) radians;
		Quat rotation = new Quat().setFromAxisAngle(rotationAxis);


		if (rotationTimeline.hasEntryAt(trackTime)) {
			Entry<Quat> entry = rotationTimeline.getEntryAt(trackTime);
			entry.getValue().mulLeft(rotation);

			if (savedLocalRotation != null) {
				savedLocalRotation.mul(rotation);
			}

			if (rotationTimeline.tans()) {
				entry.getInTan().mul(rotation);
				entry.getOutTan().mul(rotation);
			}
		}
	}

	Vec3 getUnusedAxis(byte unusedXYZ) {
		return switch (unusedXYZ) {
			case 0 -> new Vec3(1, 0, 0);
			case 1 -> new Vec3(0, -1, 0);
			default -> new Vec3(0, 0, -1);
		};
	}

}
