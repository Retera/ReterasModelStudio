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
	
	public Vec3 getRenderTranslation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedVector(animatedRenderEnvironment, "Translation", null);
	}

	public Quat getRenderRotation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedQuat(animatedRenderEnvironment, "Rotation", null);
	}

	public Vec3 getRenderScale(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedVector(animatedRenderEnvironment, "Scaling", null);
	}

	public AddKeyframeAction createTranslationKeyframe(final RenderModel renderModel, final Vec3AnimFlag translationFlag,
	                                                   final ModelStructureChangeListener structureChangeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		int trackTime = getTrackTime(renderModel);
		final int floorIndex = translationFlag.floorIndex(trackTime);
		final RenderNode renderNode = renderModel.getRenderNode(this);

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex).equals(trackTime))) {
			return null;
		} else {
			final Vec3 localLocation = renderNode.getLocalLocation();
			return getAddKeyframeAction(translationFlag, structureChangeListener, trackTime, floorIndex, localLocation);
		}
	}

	public AddKeyframeAction createRotationKeyframe(final RenderModel renderModel, final QuatAnimFlag rotationTimeline,
	                                                final ModelStructureChangeListener structureChangeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		int trackTime = getTrackTime(renderModel);
		final int floorIndex = rotationTimeline.floorIndex(trackTime);
		final RenderNode renderNode = renderModel.getRenderNode(this);

		if ((floorIndex != -1) && (rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex).equals(trackTime))) {
			return null;
		} else {
			final Quat localRotation = renderNode.getLocalRotation();
			final int insertIndex = floorIndex + 1;
			rotationTimeline.getTimes().add(insertIndex, trackTime);

			final Quat keyframeValue = new Quat(localRotation);
			rotationTimeline.getValues().add(insertIndex, keyframeValue);
//			if (rotationTimeline.tans()) {
			if (rotationTimeline.interpolationType.tangential()) {
				final Quat inTan = new Quat(localRotation);
				rotationTimeline.getInTans().add(insertIndex, inTan);

				final Quat outTan = new Quat(localRotation);
				rotationTimeline.getOutTans().add(insertIndex, outTan);

				structureChangeListener.keyframeAdded(this, rotationTimeline, trackTime);
				return new AddKeyframeAction(this, rotationTimeline, trackTime, keyframeValue, inTan, outTan, structureChangeListener);
			} else {
				structureChangeListener.keyframeAdded(this, rotationTimeline, trackTime);
				return new AddKeyframeAction(this, rotationTimeline, trackTime, keyframeValue, structureChangeListener);
			}
		}
	}

	public AddKeyframeAction createScalingKeyframe(final RenderModel renderModel, final Vec3AnimFlag scalingTimeline,
	                                               final ModelStructureChangeListener structureChangeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		int trackTime = getTrackTime(renderModel);
		final int floorIndex = scalingTimeline.floorIndex(trackTime);
		final RenderNode renderNode = renderModel.getRenderNode(this);

		if ((floorIndex != -1) && (scalingTimeline.getTimes().size() > 0) && (scalingTimeline.getTimes().get(floorIndex).equals(trackTime))) {
			return null;
		} else {
			final Vec3 localScale = renderNode.getLocalScale();
			return getAddKeyframeAction(scalingTimeline, structureChangeListener, trackTime, floorIndex, localScale);
		}
	}

	private int getTrackTime(RenderModel renderModel) {
		final int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart() + animationTime;

		final Integer globalSeq = ((TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment()).getGlobalSeq();
		if (globalSeq != null) {
			trackTime = renderModel.getAnimatedRenderEnvironment().getGlobalSeqTime(globalSeq);
		}
		return trackTime;
	}

	private AddKeyframeAction getAddKeyframeAction(Vec3AnimFlag timeline, ModelStructureChangeListener structureChangeListener, int trackTime, int floorIndex, Vec3 vec3) {
		final int insertIndex = floorIndex + 1;
		timeline.getTimes().add(insertIndex, trackTime);

		final Vec3 keyframeValue = new Vec3(vec3);
		timeline.getValues().add(insertIndex, keyframeValue);
//		if (timeline.tans()) {
		if (timeline.interpolationType.tangential()) {
			final Vec3 inTan = new Vec3(vec3);
			timeline.getInTans().add(insertIndex, inTan);

			final Vec3 outTan = new Vec3(vec3);
			timeline.getOutTans().add(insertIndex, outTan);

			structureChangeListener.keyframeAdded(this, timeline, trackTime);
			return new AddKeyframeAction(this, timeline, trackTime, keyframeValue, inTan, outTan, structureChangeListener);
		} else {
			structureChangeListener.keyframeAdded(this, timeline, trackTime);
			return new AddKeyframeAction(this, timeline, trackTime, keyframeValue, structureChangeListener);
		}
	}

	public void updateTranslationKeyframe(final RenderModel renderModel,
	                                      final double newDeltaX, final double newDeltaY, final double newDeltaZ,
	                                      final Vec3 savedLocalTranslation) {
		// Note to future author: the reason for saved local translation is that
		// we would like to be able to undo the action of moving the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must  make AnimFlag.find seek on globalSeqId
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from
		//  a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();
		final Vec3AnimFlag translationFlag = (Vec3AnimFlag) find("Translation", timeEnvironmentImpl.getGlobalSeq());
		if (translationFlag == null) {
			return;
		}
		final int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart() + animationTime;
		final Integer globalSeq = timeEnvironmentImpl.getGlobalSeq();
		if (globalSeq != null) {
			trackTime = timeEnvironmentImpl.getGlobalSeqTime(globalSeq);
		}
		final int floorIndex = translationFlag.floorIndex(trackTime);
		//final RenderNode renderNode = renderModel.getRenderNode(this);
		AnimatedNode parent = null;// = getParent();
		if (this instanceof IdObject) {
			parent = ((IdObject) this).getParent();
		}

		Vec4 translationHeap = new Vec4(0, 0, 0, 1);
		if (parent != null) {
			final RenderNode parentRenderNode = renderModel.getRenderNode(parent);

			translationHeap.transform(parentRenderNode.getWorldMatrix());
			translationHeap.add(new Vec4(newDeltaX, newDeltaY, newDeltaZ, 0));
			translationHeap.transform(Mat4.getInverted(parentRenderNode.getWorldMatrix()));
		} else {
			translationHeap.set(newDeltaX, newDeltaY, newDeltaZ, 1);
		}

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex).equals(trackTime))) {
			// we must change it
			final Vec3 oldTranslationValue = translationFlag.getValues().get(floorIndex);
			oldTranslationValue.add(translationHeap.getVec3());

			if (savedLocalTranslation != null) {
				savedLocalTranslation.add(translationHeap.getVec3());
			}

			if (translationFlag.tans()) {
				final Vec3 oldInTan = translationFlag.getInTans().get(floorIndex);
				oldInTan.add(translationHeap.getVec3());

				final Vec3 oldOutTan = translationFlag.getOutTans().get(floorIndex);
				oldOutTan.add(translationHeap.getVec3());
			}
		}

	}


	public void updateRotationKeyframe(final RenderModel renderModel, final double centerX, final double centerY,
			final double centerZ, final double radians, final byte firstXYZ, final byte secondXYZ,
			final Quat savedLocalRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be  constructed from
		//  a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();
		final QuatAnimFlag rotationTimeline = (QuatAnimFlag) find("Rotation", timeEnvironmentImpl.getGlobalSeq());
		if (rotationTimeline == null) {
			return;
		}
		final int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart() + animationTime;
		final Integer globalSeq = timeEnvironmentImpl.getGlobalSeq();
		if (globalSeq != null) {
			trackTime = timeEnvironmentImpl.getGlobalSeqTime(globalSeq);
		}
		final int floorIndex = rotationTimeline.floorIndex(trackTime);
		//final RenderNode renderNode = renderModel.getRenderNode(this);
		final byte unusedXYZ = CoordinateSystem.Util.getUnusedXYZ(firstXYZ, secondXYZ);
		AnimatedNode parent = null;// = getParent();
		if (this instanceof IdObject) {
			parent = ((IdObject) this).getParent();
		}

		Vec4 axisAngleHeap = new Vec4(0, 0, 0, 1);

		if (parent != null) {
			final RenderNode parentRenderNode = renderModel.getRenderNode(parent);

			axisAngleHeap.transform(parentRenderNode.getWorldMatrix());

			switch (unusedXYZ) {
				case 0 -> axisAngleHeap.set(axisAngleHeap.x + 1, axisAngleHeap.y + 0, axisAngleHeap.z + 0, 1);
				case 1 -> axisAngleHeap.set(axisAngleHeap.x + 0, axisAngleHeap.y + -1, axisAngleHeap.z + 0, 1);
				case 2 -> axisAngleHeap.set(axisAngleHeap.x + 0, axisAngleHeap.y + 0, axisAngleHeap.z + -1, 1);
			}

			axisAngleHeap.transform(Mat4.getInverted(parentRenderNode.getWorldMatrix()));
		} else {
			switch (unusedXYZ) {
				case 0 -> axisAngleHeap.set(1, 0, 0, 1);
				case 1 -> axisAngleHeap.set(0, -1, 0, 1);
				case 2 -> axisAngleHeap.set(0, 0, -1, 1);
			}
		}
		axisAngleHeap.w = (float)radians;
		Quat rotationDeltaHeap = new Quat().setFromAxisAngle(axisAngleHeap);

		if ((floorIndex != -1) && (rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex).equals(trackTime))) {
			// we must change it
			final Quat oldTranslationValue = rotationTimeline.getValues().get(floorIndex);
			oldTranslationValue.mulLeft(rotationDeltaHeap);

			if (savedLocalRotation != null) {
				savedLocalRotation.mul(rotationDeltaHeap);
			}

			if (rotationTimeline.tans()) {
				final Quat oldInTan = rotationTimeline.getInTans().get(floorIndex);
				oldInTan.mul(rotationDeltaHeap);

				final Quat oldOutTan = rotationTimeline.getOutTans().get(floorIndex);
				oldOutTan.mul(rotationDeltaHeap);
			}
		}
	}

	public void updateScalingKeyframe(final RenderModel renderModel,
	                                  final double scaleX, final double scaleY, final double scaleZ,
	                                  final Vec3 savedLocalScaling) {
		updateScalingKeyframe(renderModel, new Vec3(scaleX, scaleY, scaleZ), savedLocalScaling);
	}

	public void updateScalingKeyframe(final RenderModel renderModel, final Vec3 scale, final Vec3 savedLocalScaling) {
		// Note to future author: the reason for saved local scaling is that
		// we would like to be able to undo the action of moving the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from
		//  a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();
		final Vec3AnimFlag translationFlag = (Vec3AnimFlag) find("Scaling", timeEnvironmentImpl.getGlobalSeq());
		if (translationFlag == null) {
			return;
		}

		final int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart() + animationTime;

		final Integer globalSeq = timeEnvironmentImpl.getGlobalSeq();
		if (globalSeq != null) {
			trackTime = timeEnvironmentImpl.getGlobalSeqTime(globalSeq);
		}
		final int floorIndex = translationFlag.floorIndex(trackTime);

		Vec3 translationHeap = new Vec3(scale);

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex).equals(trackTime))) {
			// we must change it
			final Vec3 oldTranslationValue = translationFlag.getValues().get(floorIndex);
			oldTranslationValue.multiply(translationHeap);

			if (savedLocalScaling != null) {
				savedLocalScaling.multiply(translationHeap);
			}

			if (translationFlag.tans()) {
				final Vec3 oldInTan = translationFlag.getInTans().get(floorIndex);
				oldInTan.multiply(translationHeap);

				final Vec3 oldOutTan = translationFlag.getOutTans().get(floorIndex);
				oldOutTan.multiply(translationHeap);
			}
		}
	}

	public void updateLocalRotationKeyframe(final int trackTime, final Integer trackGlobalSeq, final Quat localRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		final QuatAnimFlag rotationTimeline = (QuatAnimFlag) find("Rotation", trackGlobalSeq);
//		final AnimFlag rotationTimeline = find("Rotation", trackGlobalSeq);
		if (rotationTimeline == null) {
			return;
		}
		final int floorIndex = rotationTimeline.floorIndex(trackTime);

		if ((floorIndex != -1) && (rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex).equals(trackTime))) {
			// we must change it
			final Quat oldTranslationValue = rotationTimeline.getValues().get(floorIndex);
			oldTranslationValue.mul(localRotation);

			if (rotationTimeline.tans()) {
				final Quat oldInTan = rotationTimeline.getInTans().get(floorIndex);
				oldInTan.mul(localRotation);

				final Quat oldOutTan = rotationTimeline.getOutTans().get(floorIndex);
				oldOutTan.mul(localRotation);
			}
		}
	}

	public void updateLocalRotationKeyframeInverse(final int trackTime, final Integer trackGlobalSeq, final Quat localRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		final QuatAnimFlag rotationTimeline = (QuatAnimFlag) find("Rotation", trackGlobalSeq);
		if (rotationTimeline == null) {
			return;
		}
		final int floorIndex = rotationTimeline.floorIndex(trackTime);

		if ((floorIndex != -1) && (rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex).equals(trackTime))) {
			// we must change it
			final Quat oldTranslationValue = rotationTimeline.getValues().get(floorIndex);
			rotateStuff(localRotation, oldTranslationValue);

			if (rotationTimeline.tans()) {
				final Quat oldInTan = rotationTimeline.getInTans().get(floorIndex);
				rotateStuff(localRotation, oldInTan);

				final Quat oldOutTan = rotationTimeline.getOutTans().get(floorIndex);
				rotateStuff(localRotation, oldOutTan);
			}
		}
	}

	public void updateLocalTranslationKeyframe(final int trackTime, final Integer trackGlobalSeq,
			final double newDeltaX, final double newDeltaY, final double newDeltaZ) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		final Vec3AnimFlag translationFlag = (Vec3AnimFlag) find("Translation", trackGlobalSeq);
		if (translationFlag == null) {
			return;
		}
		final int floorIndex = translationFlag.floorIndex(trackTime);

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex).equals(trackTime))) {
			// we must change it
			final Vec3 oldTranslationValue = translationFlag.getValues().get(floorIndex);
			oldTranslationValue.add(new Vec3(newDeltaX, newDeltaY, newDeltaZ));

			if (translationFlag.tans()) {
				final Vec3 oldInTan = translationFlag.getInTans().get(floorIndex);
				oldInTan.add(new Vec3(newDeltaX, newDeltaY, newDeltaZ));

				final Vec3 oldOutTan = translationFlag.getOutTans().get(floorIndex);
				oldOutTan.add(new Vec3(newDeltaX, newDeltaY, newDeltaZ));
			}
		}

	}

	public void updateLocalScalingKeyframe(final int trackTime, final Integer trackGlobalSeq, final Vec3 localScaling) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		final Vec3AnimFlag translationFlag = (Vec3AnimFlag) find("Scaling", trackGlobalSeq);
		if (translationFlag == null) {
			return;
		}
		final int floorIndex = translationFlag.floorIndex(trackTime);

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex).equals(trackTime))) {
			// we must change it
			final Vec3 oldTranslationValue = translationFlag.getValues().get(floorIndex);
			oldTranslationValue.multiply(localScaling);

			if (translationFlag.tans()) {
				final Vec3 oldInTan = translationFlag.getInTans().get(floorIndex);
				oldInTan.multiply(localScaling);

				final Vec3 oldOutTan = translationFlag.getOutTans().get(floorIndex);
				oldOutTan.multiply(localScaling);
			}
		}

	}


	private void rotateStuff(Quat localRotation, Quat heap) {
		Quat rotationDeltaHeap = new Quat().setIdentity().mulInverse(localRotation);
		heap.mul(rotationDeltaHeap);

	}
}
