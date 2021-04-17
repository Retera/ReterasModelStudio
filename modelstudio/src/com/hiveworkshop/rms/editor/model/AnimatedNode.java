package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation.AddKeyframeAction;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.util.List;

public abstract class AnimatedNode extends TimelineContainer {

//	abstract public AnimatedNode getParent();

	abstract public Vec3 getPivotPoint();

	abstract public List<? extends AnimatedNode> getChildrenNodes();

	abstract public String getName();

	public Vec3 getRenderTranslation(AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedVector(animatedRenderEnvironment, "Translation", null);
	}

	public Quat getRenderRotation(AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedQuat(animatedRenderEnvironment, "Rotation", null);
	}

	public Vec3 getRenderScale(AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedVector(animatedRenderEnvironment, "Scaling", null);
	}

	public AddKeyframeAction createTranslationKeyframe(RenderModel renderModel, Vec3AnimFlag translationFlag,
	                                                   ModelStructureChangeListener changeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		int trackTime = getTrackTime(renderModel);
		int floorIndex = translationFlag.floorIndex(trackTime);
		RenderNode renderNode = renderModel.getRenderNode(this);

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex).equals(trackTime))) {
			return null;
		} else {
			Vec3 localLocation = renderNode.getLocalLocation();
			return getAddKeyframeAction(translationFlag, changeListener, trackTime, floorIndex, localLocation);
		}
	}

	public AddKeyframeAction createRotationKeyframe(RenderModel renderModel, QuatAnimFlag rotationTimeline,
	                                                ModelStructureChangeListener changeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		int trackTime = getTrackTime(renderModel);
		int floorIndex = rotationTimeline.floorIndex(trackTime);
		RenderNode renderNode = renderModel.getRenderNode(this);

		if ((floorIndex != -1) && (rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex).equals(trackTime))) {
			return null;
		} else {
			Quat localRotation = renderNode.getLocalRotation();
			int insertIndex = floorIndex + 1;
			rotationTimeline.getTimes().add(insertIndex, trackTime);

			Quat keyframeValue = new Quat(localRotation);
			rotationTimeline.getValues().add(insertIndex, keyframeValue);
			if (rotationTimeline.interpolationType.tangential()) {
				Quat inTan = new Quat(localRotation);
				rotationTimeline.getInTans().add(insertIndex, inTan);

				Quat outTan = new Quat(localRotation);
				rotationTimeline.getOutTans().add(insertIndex, outTan);

				changeListener.keyframeAdded(this, rotationTimeline, trackTime);
				return new AddKeyframeAction(this, rotationTimeline, trackTime, keyframeValue, inTan, outTan, changeListener);
			} else {
				changeListener.keyframeAdded(this, rotationTimeline, trackTime);
				return new AddKeyframeAction(this, rotationTimeline, trackTime, keyframeValue, changeListener);
			}
		}
	}

	public AddKeyframeAction createScalingKeyframe(RenderModel renderModel, Vec3AnimFlag scalingTimeline,
	                                               ModelStructureChangeListener changeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		int trackTime = getTrackTime(renderModel);
		int floorIndex = scalingTimeline.floorIndex(trackTime);
		RenderNode renderNode = renderModel.getRenderNode(this);

		if ((floorIndex != -1) && (scalingTimeline.getTimes().size() > 0) && (scalingTimeline.getTimes().get(floorIndex).equals(trackTime))) {
			return null;
		} else {
			Vec3 localScale = renderNode.getLocalScale();
			return getAddKeyframeAction(scalingTimeline, changeListener, trackTime, floorIndex, localScale);
		}
	}

	private int getTrackTime(RenderModel renderModel) {
		int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart() + animationTime;

		Integer globalSeq = ((TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment()).getGlobalSeq();
		if (globalSeq != null) {
			trackTime = renderModel.getAnimatedRenderEnvironment().getGlobalSeqTime(globalSeq);
		}
		return trackTime;
	}

	private AddKeyframeAction getAddKeyframeAction(Vec3AnimFlag timeline, ModelStructureChangeListener changeListener,
	                                               int trackTime, int floorIndex, Vec3 vec3) {
		int insertIndex = floorIndex + 1;
		timeline.getTimes().add(insertIndex, trackTime);

		Vec3 keyframeValue = new Vec3(vec3);
		timeline.getValues().add(insertIndex, keyframeValue);
//		if (timeline.tans()) {
		if (timeline.interpolationType.tangential()) {
			Vec3 inTan = new Vec3(vec3);
			timeline.getInTans().add(insertIndex, inTan);

			Vec3 outTan = new Vec3(vec3);
			timeline.getOutTans().add(insertIndex, outTan);

			changeListener.keyframeAdded(this, timeline, trackTime);
			return new AddKeyframeAction(this, timeline, trackTime, keyframeValue, inTan, outTan, changeListener);
		} else {
			changeListener.keyframeAdded(this, timeline, trackTime);
			return new AddKeyframeAction(this, timeline, trackTime, keyframeValue, changeListener);
		}
	}

	public void updateTranslationKeyframe(RenderModel renderModel,
	                                      double newDeltaX, double newDeltaY, double newDeltaZ,
	                                      Vec3 savedLocalTranslation) {
		// Note to future author: the reason for saved local translation is that
		// we would like to be able to undo the action of moving the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must  make AnimFlag.find seek on globalSeqId
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from
		//  a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();
		Vec3AnimFlag translationFlag = (Vec3AnimFlag) find("Translation", timeEnvironmentImpl.getGlobalSeq());
		if (translationFlag == null) {
			return;
		}
		int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart() + animationTime;
		Integer globalSeq = timeEnvironmentImpl.getGlobalSeq();
		if (globalSeq != null) {
			trackTime = timeEnvironmentImpl.getGlobalSeqTime(globalSeq);
		}
		int floorIndex = translationFlag.floorIndex(trackTime);
		//final RenderNode renderNode = renderModel.getRenderNode(this);
		AnimatedNode parent = null;// = getParent();
		if (this instanceof IdObject) {
			parent = ((IdObject) this).getParent();
		}

		Vec4 translationHeap = new Vec4(0, 0, 0, 1);
		if (parent != null) {
			RenderNode parentRenderNode = renderModel.getRenderNode(parent);

			translationHeap.transform(parentRenderNode.getWorldMatrix());
			translationHeap.add(new Vec4(newDeltaX, newDeltaY, newDeltaZ, 0));
			translationHeap.transform(Mat4.getInverted(parentRenderNode.getWorldMatrix()));
		} else {
			translationHeap.set(newDeltaX, newDeltaY, newDeltaZ, 1);
		}

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex).equals(trackTime))) {
			// we must change it
			translationFlag.getValues().get(floorIndex).add(translationHeap.getVec3());

			if (savedLocalTranslation != null) {
				savedLocalTranslation.add(translationHeap.getVec3());
			}

			if (translationFlag.tans()) {
				translationFlag.getInTans().get(floorIndex).add(translationHeap.getVec3());
				translationFlag.getOutTans().get(floorIndex).add(translationHeap.getVec3());
			}
		}

	}


	public void updateRotationKeyframe(RenderModel renderModel, double centerX, double centerY, double centerZ,
	                                   double radians, byte firstXYZ, byte secondXYZ, Quat savedLocalRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be  constructed from
		//  a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();
		QuatAnimFlag rotationTimeline = (QuatAnimFlag) find("Rotation", timeEnvironmentImpl.getGlobalSeq());
		if (rotationTimeline == null) {
			return;
		}
		int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart() + animationTime;
		Integer globalSeq = timeEnvironmentImpl.getGlobalSeq();
		if (globalSeq != null) {
			trackTime = timeEnvironmentImpl.getGlobalSeqTime(globalSeq);
		}
		int floorIndex = rotationTimeline.floorIndex(trackTime);
		//final RenderNode renderNode = renderModel.getRenderNode(this);
		byte unusedXYZ = CoordinateSystem.Util.getUnusedXYZ(firstXYZ, secondXYZ);
		AnimatedNode parent = null;// = getParent();
		if (this instanceof IdObject) {
			parent = ((IdObject) this).getParent();
		}

		Vec4 rotationAxis = new Vec4(0, 0, 0, 1);

		if (parent != null) {
			RenderNode parentRenderNode = renderModel.getRenderNode(parent);

			rotationAxis.transform(parentRenderNode.getWorldMatrix());
			rotationAxis.add(getUnusedAxis(unusedXYZ));
			rotationAxis.transform(Mat4.getInverted(parentRenderNode.getWorldMatrix()));
		} else {
			rotationAxis.add(getUnusedAxis(unusedXYZ));
		}
		rotationAxis.w = (float) radians;
		Quat rotation = new Quat().setFromAxisAngle(rotationAxis);

		if ((floorIndex != -1) && (rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex).equals(trackTime))) {
			// we must change it
			rotationTimeline.getValues().get(floorIndex).mulLeft(rotation);

//			Quat oldTranslationValue = Quat.getProd(rotation, rotationTimeline.getValues().get(floorIndex));

			if (savedLocalRotation != null) {
				savedLocalRotation.mul(rotation);
			}

			if (rotationTimeline.tans()) {
				rotationTimeline.getInTans().get(floorIndex).mul(rotation);
				rotationTimeline.getOutTans().get(floorIndex).mul(rotation);
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

	public void updateScalingKeyframe(RenderModel renderModel,
	                                  double scaleX, double scaleY, double scaleZ,
	                                  Vec3 savedLocalScaling) {
		updateScalingKeyframe(renderModel, new Vec3(scaleX, scaleY, scaleZ), savedLocalScaling);
	}

	public void updateScalingKeyframe(RenderModel renderModel, Vec3 scale, Vec3 savedLocalScaling) {
		// Note to future author: the reason for saved local scaling is that
		// we would like to be able to undo the action of moving the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from
		//  a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();
		Vec3AnimFlag translationFlag = (Vec3AnimFlag) find("Scaling", timeEnvironmentImpl.getGlobalSeq());
		if (translationFlag == null) {
			return;
		}

		int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart() + animationTime;

		Integer globalSeq = timeEnvironmentImpl.getGlobalSeq();
		if (globalSeq != null) {
			trackTime = timeEnvironmentImpl.getGlobalSeqTime(globalSeq);
		}
		int floorIndex = translationFlag.floorIndex(trackTime);

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex).equals(trackTime))) {
			// we must change it
			translationFlag.getValues().get(floorIndex).multiply(scale);

			if (savedLocalScaling != null) {
				savedLocalScaling.multiply(scale);
			}

			if (translationFlag.tans()) {
				translationFlag.getInTans().get(floorIndex).multiply(scale);
				translationFlag.getOutTans().get(floorIndex).multiply(scale);
			}
		}
	}

	public void updateLocalRotationKeyframe(int trackTime, Integer trackGlobalSeq, Quat localRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		QuatAnimFlag rotationTimeline = (QuatAnimFlag) find("Rotation", trackGlobalSeq);
//		final AnimFlag rotationTimeline = find("Rotation", trackGlobalSeq);
		if (rotationTimeline == null) {
			return;
		}
		int floorIndex = rotationTimeline.floorIndex(trackTime);

		if ((floorIndex != -1) && (rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex).equals(trackTime))) {
			// we must change it
			rotationTimeline.getValues().get(floorIndex).mul(localRotation);

			if (rotationTimeline.tans()) {
				rotationTimeline.getInTans().get(floorIndex).mul(localRotation);
				rotationTimeline.getOutTans().get(floorIndex).mul(localRotation);

			}
		}
	}

	public void updateLocalRotationKeyframeInverse(int trackTime, Integer trackGlobalSeq, Quat localRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		QuatAnimFlag rotationTimeline = (QuatAnimFlag) find("Rotation", trackGlobalSeq);
		if (rotationTimeline == null) {
			return;
		}
		int floorIndex = rotationTimeline.floorIndex(trackTime);

		if ((floorIndex != -1) && (rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex).equals(trackTime))) {
			// we must change it
			rotationTimeline.getValues().get(floorIndex).mulLeft(localRotation.getInverted());

			if (rotationTimeline.tans()) {
				rotationTimeline.getInTans().get(floorIndex).mulLeft(localRotation.getInverted());
				rotationTimeline.getOutTans().get(floorIndex).mulLeft(localRotation.getInverted());
			}

		}
	}

	public void updateLocalTranslationKeyframe(int trackTime, Integer trackGlobalSeq,
	                                           double newDeltaX, double newDeltaY, double newDeltaZ) {
		updateLocalTranslationKeyframe(trackTime, trackGlobalSeq, new Vec3(newDeltaX, newDeltaY, newDeltaZ));
	}

	public void updateLocalTranslationKeyframe(int trackTime, Integer trackGlobalSeq, Vec3 localTranslation) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		Vec3AnimFlag translationFlag = (Vec3AnimFlag) find("Translation", trackGlobalSeq);
		if (translationFlag == null) {
			return;
		}
		int floorIndex = translationFlag.floorIndex(trackTime);

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex).equals(trackTime))) {
			// we must change it
			translationFlag.getValues().get(floorIndex).add(localTranslation);

			if (translationFlag.tans()) {
				translationFlag.getInTans().get(floorIndex).add(localTranslation);
				translationFlag.getOutTans().get(floorIndex).add(localTranslation);
			}
		}
	}

	public void updateLocalScalingKeyframe(int trackTime, Integer trackGlobalSeq, Vec3 localScaling) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		Vec3AnimFlag translationFlag = (Vec3AnimFlag) find("Scaling", trackGlobalSeq);
		if (translationFlag == null) {
			return;
		}
		int floorIndex = translationFlag.floorIndex(trackTime);

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex).equals(trackTime))) {
			// we must change it
			translationFlag.getValues().get(floorIndex).multiply(localScaling);

			if (translationFlag.tans()) {
				translationFlag.getInTans().get(floorIndex).multiply(localScaling);
				translationFlag.getOutTans().get(floorIndex).multiply(localScaling);
			}
		}
	}
}
