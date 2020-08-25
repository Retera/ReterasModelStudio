package com.hiveworkshop.rms.editor.model;

import java.util.List;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation.AddKeyframeAction;

public abstract class AnimatedNode extends TimelineContainer {
	private static final Vertex4 translationHeap = new Vertex4();
	private static final Matrix4 matrixHeap = new Matrix4();
	private static final QuaternionRotation rotationHeap = new QuaternionRotation();
	private static final QuaternionRotation rotationDeltaHeap = new QuaternionRotation();
	private static final Vertex4 axisAngleHeap = new Vertex4();

	abstract public AnimatedNode getParent();

	abstract public Vertex getPivotPoint();

	abstract public List<? extends AnimatedNode> getChildrenNodes();

	abstract public String getName();
	
	public Vertex getRenderTranslation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedVector(animatedRenderEnvironment, "Translation", null);
	}

	public QuaternionRotation getRenderRotation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedQuat(animatedRenderEnvironment, "Rotation", null);
	}

	public Vertex getRenderScale(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedVector(animatedRenderEnvironment, "Scaling", null);
	}

	public AddKeyframeAction createTranslationKeyframe(final RenderModel renderModel, final AnimFlag translationFlag,
                                                       final ModelStructureChangeListener structureChangeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart() + animationTime;
		final Integer globalSeq = ((TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment()).getGlobalSeq();
		if (globalSeq != null) {
			trackTime = renderModel.getAnimatedRenderEnvironment().getGlobalSeqTime(globalSeq);
		}
		final int floorIndex = translationFlag.floorIndex(trackTime);
		final RenderNode renderNode = renderModel.getRenderNode(this);

		if ((translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex) == trackTime)) {
			return null;
		} else {
			final Vertex localLocation = renderNode.getLocalLocation();
			final int insertIndex = ((translationFlag.getTimes().size() == 0)
					|| (translationFlag.getTimes().get(0) > trackTime)) ? 0 : floorIndex + 1;
			translationFlag.getTimes().add(insertIndex, trackTime);
			final Vertex keyframeValue = new Vertex(localLocation.x, localLocation.y, localLocation.z);
			translationFlag.getValues().add(insertIndex, keyframeValue);
			if (translationFlag.tans()) {
				final Vertex inTan = new Vertex(localLocation.x, localLocation.y, localLocation.z);
				translationFlag.getInTans().add(insertIndex, inTan);
				final Vertex outTan = new Vertex(localLocation.x, localLocation.y, localLocation.z);
				translationFlag.getOutTans().add(insertIndex, outTan);
				structureChangeListener.keyframeAdded(this, translationFlag, trackTime);
				return new AddKeyframeAction(this, translationFlag, trackTime, keyframeValue, inTan, outTan,
						structureChangeListener);
			} else {
				structureChangeListener.keyframeAdded(this, translationFlag, trackTime);
				return new AddKeyframeAction(this, translationFlag, trackTime, keyframeValue, structureChangeListener);
			}
		}
	}

	public AddKeyframeAction createRotationKeyframe(final RenderModel renderModel, final AnimFlag rotationTimeline,
			final ModelStructureChangeListener structureChangeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart() + animationTime;
		final Integer globalSeq = ((TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment()).getGlobalSeq();
		if (globalSeq != null) {
			trackTime = renderModel.getAnimatedRenderEnvironment().getGlobalSeqTime(globalSeq);
		}
		final int floorIndex = rotationTimeline.floorIndex(trackTime);
		final RenderNode renderNode = renderModel.getRenderNode(this);

		if ((rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex) == trackTime)) {
			return null;
		} else {
			final QuaternionRotation localRotation = renderNode.getLocalRotation();
			final int insertIndex = ((rotationTimeline.getTimes().size() == 0)
					|| (rotationTimeline.getTimes().get(0) > trackTime)) ? 0 : floorIndex + 1;
			rotationTimeline.getTimes().add(insertIndex, trackTime);
			final QuaternionRotation keyframeValue = new QuaternionRotation(localRotation.x, localRotation.y,
					localRotation.z, localRotation.w);
			rotationTimeline.getValues().add(insertIndex, keyframeValue);
			if (rotationTimeline.tans()) {
				final QuaternionRotation inTan = new QuaternionRotation(localRotation.x, localRotation.y,
						localRotation.z, localRotation.w);
				rotationTimeline.getInTans().add(insertIndex, inTan);
				final QuaternionRotation outTan = new QuaternionRotation(localRotation.x, localRotation.y,
						localRotation.z, localRotation.w);
				rotationTimeline.getOutTans().add(insertIndex, outTan);
				structureChangeListener.keyframeAdded(this, rotationTimeline, trackTime);
				return new AddKeyframeAction(this, rotationTimeline, trackTime, keyframeValue, inTan, outTan,
						structureChangeListener);
			} else {
				structureChangeListener.keyframeAdded(this, rotationTimeline, trackTime);
				return new AddKeyframeAction(this, rotationTimeline, trackTime, keyframeValue, structureChangeListener);
			}
		}
	}

	public AddKeyframeAction createScalingKeyframe(final RenderModel renderModel, final AnimFlag scalingTimeline,
			final ModelStructureChangeListener structureChangeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart() + animationTime;
		final Integer globalSeq = ((TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment()).getGlobalSeq();
		if (globalSeq != null) {
			trackTime = renderModel.getAnimatedRenderEnvironment().getGlobalSeqTime(globalSeq);
		}
		final int floorIndex = scalingTimeline.floorIndex(trackTime);
		final RenderNode renderNode = renderModel.getRenderNode(this);

		if ((scalingTimeline.getTimes().size() > 0) && (scalingTimeline.getTimes().get(floorIndex) == trackTime)) {
			return null;
		} else {
			final Vertex localScale = renderNode.getLocalScale();
			final int insertIndex = ((scalingTimeline.getTimes().size() == 0)
					|| (scalingTimeline.getTimes().get(0) > trackTime)) ? 0 : floorIndex + 1;
			scalingTimeline.getTimes().add(insertIndex, trackTime);
			final Vertex keyframeValue = new Vertex(localScale.x, localScale.y, localScale.z);
			scalingTimeline.getValues().add(insertIndex, keyframeValue);
			if (scalingTimeline.tans()) {
				final Vertex inTan = new Vertex(localScale.x, localScale.y, localScale.z);
				scalingTimeline.getInTans().add(insertIndex, inTan);
				final Vertex outTan = new Vertex(localScale.x, localScale.y, localScale.z);
				scalingTimeline.getOutTans().add(insertIndex, outTan);
				structureChangeListener.keyframeAdded(this, scalingTimeline, trackTime);
				return new AddKeyframeAction(this, scalingTimeline, trackTime, keyframeValue, inTan, outTan,
						structureChangeListener);
			} else {
				structureChangeListener.keyframeAdded(this, scalingTimeline, trackTime);
				return new AddKeyframeAction(this, scalingTimeline, trackTime, keyframeValue, structureChangeListener);
			}
		}
	}

	public void updateTranslationKeyframe(final RenderModel renderModel, final double newDeltaX, final double newDeltaY,
			final double newDeltaZ, final Vertex savedLocalTranslation) {
		// Note to future author: the reason for saved local translation is that
		// we would like to be able to undo the action of moving the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be
		// constructed from
		// a TimeEnvironmentImpl render environment, and never from the anim previewer
		// impl
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel
				.getAnimatedRenderEnvironment();
		final AnimFlag translationFlag = find("Translation", timeEnvironmentImpl.getGlobalSeq());
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
		final AnimatedNode parent = getParent();
		if (parent != null) {
			final RenderNode parentRenderNode = renderModel.getRenderNode(parent);
			Matrix4.invert(parentRenderNode.getWorldMatrix(), matrixHeap);

			translationHeap.x = 0;
			translationHeap.y = 0;
			translationHeap.z = 0;
			translationHeap.w = 1;

			Matrix4.transform(parentRenderNode.getWorldMatrix(), translationHeap, translationHeap);

			translationHeap.x = (float) (translationHeap.x + newDeltaX);
			translationHeap.y = (float) (translationHeap.y + newDeltaY);
			translationHeap.z = (float) (translationHeap.z + newDeltaZ);
			translationHeap.w = 1;

			Matrix4.transform(matrixHeap, translationHeap, translationHeap);
		} else {
			translationHeap.x = (float) (newDeltaX);
			translationHeap.y = (float) (newDeltaY);
			translationHeap.z = (float) (newDeltaZ);
			translationHeap.w = 1;
		}

		if ((translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex) == trackTime)) {
			// we must change it
			final Vertex oldTranslationValue = (Vertex) translationFlag.getValues().get(floorIndex);
			oldTranslationValue.x += translationHeap.x;
			oldTranslationValue.y += translationHeap.y;
			oldTranslationValue.z += translationHeap.z;

			if (savedLocalTranslation != null) {
				savedLocalTranslation.x += translationHeap.x;
				savedLocalTranslation.y += translationHeap.y;
				savedLocalTranslation.z += translationHeap.z;
			}

			if (translationFlag.tans()) {
				final Vertex oldInTan = (Vertex) translationFlag.getInTans().get(floorIndex);
				oldInTan.x += translationHeap.x;
				oldInTan.y += translationHeap.y;
				oldInTan.z += translationHeap.z;

				final Vertex oldOutTan = (Vertex) translationFlag.getOutTans().get(floorIndex);
				oldOutTan.x += translationHeap.x;
				oldOutTan.y += translationHeap.y;
				oldOutTan.z += translationHeap.z;
			}
		}

	}

	public void updateRotationKeyframe(final RenderModel renderModel, final double centerX, final double centerY,
			final double centerZ, final double radians, final byte firstXYZ, final byte secondXYZ,
			final QuaternionRotation savedLocalRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be
		// constructed from
		// a TimeEnvironmentImpl render environment, and never from the anim previewer
		// impl
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel
				.getAnimatedRenderEnvironment();
		final AnimFlag rotationTimeline = find("Rotation", timeEnvironmentImpl.getGlobalSeq());
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
		final AnimatedNode parent = getParent();
		if (parent != null) {
			final RenderNode parentRenderNode = renderModel.getRenderNode(parent);
			Matrix4.invert(parentRenderNode.getWorldMatrix(), matrixHeap);

			axisAngleHeap.x = 0;
			axisAngleHeap.y = 0;
			axisAngleHeap.z = 0;
			axisAngleHeap.w = 1;

			Matrix4.transform(parentRenderNode.getWorldMatrix(), axisAngleHeap, axisAngleHeap);

			switch (unusedXYZ) {
			case 0:
				axisAngleHeap.x = axisAngleHeap.x + 1;
				axisAngleHeap.y = axisAngleHeap.y + 0;
				axisAngleHeap.z = axisAngleHeap.z + 0;
				break;
			case 1:
				axisAngleHeap.x = axisAngleHeap.x + 0;
				axisAngleHeap.y = axisAngleHeap.y + -1;
				axisAngleHeap.z = axisAngleHeap.z + 0;
				break;
			case 2:
				axisAngleHeap.x = axisAngleHeap.x + 0;
				axisAngleHeap.y = axisAngleHeap.y + 0;
				axisAngleHeap.z = axisAngleHeap.z + -1;
				break;
			}
			axisAngleHeap.w = 1;

			Matrix4.transform(matrixHeap, axisAngleHeap, axisAngleHeap);
		} else {
			switch (unusedXYZ) {
			case 0:
				axisAngleHeap.x = 1;
				axisAngleHeap.y = 0;
				axisAngleHeap.z = 0;
				break;
			case 1:
				axisAngleHeap.x = 0;
				axisAngleHeap.y = -1;
				axisAngleHeap.z = 0;
				break;
			case 2:
				axisAngleHeap.x = 0;
				axisAngleHeap.y = 0;
				axisAngleHeap.z = -1;
				break;
			}
		}
		axisAngleHeap.w = (float) radians;
		rotationDeltaHeap.setFromAxisAngle(axisAngleHeap);

		if ((rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex) == trackTime)) {
			// we must change it
			final QuaternionRotation oldTranslationValue = (QuaternionRotation) rotationTimeline.getValues()
					.get(floorIndex);
			rotationHeap.x = (float) oldTranslationValue.x;
			rotationHeap.y = (float) oldTranslationValue.y;
			rotationHeap.z = (float) oldTranslationValue.z;
			rotationHeap.w = (float) oldTranslationValue.w;
			QuaternionRotation.mul(rotationDeltaHeap, rotationHeap, rotationHeap);

			oldTranslationValue.x = rotationHeap.x;
			oldTranslationValue.y = rotationHeap.y;
			oldTranslationValue.z = rotationHeap.z;
			oldTranslationValue.w = rotationHeap.w;

			if (savedLocalRotation != null) {
				QuaternionRotation.mul(savedLocalRotation, rotationDeltaHeap, savedLocalRotation);
			}

			if (rotationTimeline.tans()) {
				final QuaternionRotation oldInTan = (QuaternionRotation) rotationTimeline.getInTans().get(floorIndex);
				rotationHeap.x = (float) oldInTan.x;
				rotationHeap.y = (float) oldInTan.y;
				rotationHeap.z = (float) oldInTan.z;
				rotationHeap.w = (float) oldInTan.w;
				QuaternionRotation.mul(rotationDeltaHeap, rotationHeap, rotationHeap);
				oldInTan.x = rotationHeap.x;
				oldInTan.y = rotationHeap.y;
				oldInTan.z = rotationHeap.z;
				oldInTan.w = rotationHeap.w;

				final QuaternionRotation oldOutTan = (QuaternionRotation) rotationTimeline.getOutTans().get(floorIndex);
				rotationHeap.x = (float) oldOutTan.x;
				rotationHeap.y = (float) oldOutTan.y;
				rotationHeap.z = (float) oldOutTan.z;
				rotationHeap.w = (float) oldOutTan.w;
				QuaternionRotation.mul(rotationDeltaHeap, rotationHeap, rotationHeap);
				oldOutTan.x = rotationHeap.x;
				oldOutTan.y = rotationHeap.y;
				oldOutTan.z = rotationHeap.z;
				oldOutTan.w = rotationHeap.w;
			}
		}
	}

	public void updateScalingKeyframe(final RenderModel renderModel, final double scaleX, final double scaleY,
			final double scaleZ, final Vertex savedLocalScaling) {
		// Note to future author: the reason for saved local scaling is that
		// we would like to be able to undo the action of moving the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be
		// constructed from
		// a TimeEnvironmentImpl render environment, and never from the anim previewer
		// impl
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel
				.getAnimatedRenderEnvironment();
		final AnimFlag translationFlag = find("Scaling", timeEnvironmentImpl.getGlobalSeq());
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
		// final RenderNode renderNode = renderModel.getRenderNode(this);
		// if (parent != null) {
		// final RenderNode parentRenderNode = renderModel.getRenderNode(parent);
		// translationHeap.x = (float)scaleX *
		// parentRenderNode.getInverseWorldScale().x;
		// translationHeap.y = (float)scaleY *
		// parentRenderNode.getInverseWorldScale().y;
		// translationHeap.z = (float)scaleZ *
		// parentRenderNode.getInverseWorldScale().z;
		// translationHeap.w = 1;
		// } else {
		translationHeap.x = (float) scaleX;
		translationHeap.y = (float) scaleY;
		translationHeap.z = (float) scaleZ;
		// translationHeap.w = 1;
		// }

		if ((translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex) == trackTime)) {
			// we must change it
			final Vertex oldTranslationValue = (Vertex) translationFlag.getValues().get(floorIndex);
			oldTranslationValue.x *= translationHeap.x;
			oldTranslationValue.y *= translationHeap.y;
			oldTranslationValue.z *= translationHeap.z;

			if (savedLocalScaling != null) {
				savedLocalScaling.x *= translationHeap.x;
				savedLocalScaling.y *= translationHeap.y;
				savedLocalScaling.z *= translationHeap.z;
			}

			if (translationFlag.tans()) {
				final Vertex oldInTan = (Vertex) translationFlag.getInTans().get(floorIndex);
				oldInTan.x *= translationHeap.x;
				oldInTan.y *= translationHeap.y;
				oldInTan.z *= translationHeap.z;

				final Vertex oldOutTan = (Vertex) translationFlag.getOutTans().get(floorIndex);
				oldOutTan.x *= translationHeap.x;
				oldOutTan.y *= translationHeap.y;
				oldOutTan.z *= translationHeap.z;
			}
		}
	}

	public void updateLocalRotationKeyframe(final int trackTime, final Integer trackGlobalSeq,
			final QuaternionRotation localRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final AnimFlag rotationTimeline = find("Rotation", trackGlobalSeq);
		if (rotationTimeline == null) {
			return;
		}
		final int floorIndex = rotationTimeline.floorIndex(trackTime);

		if ((rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex) == trackTime)) {
			// we must change it
			final QuaternionRotation oldTranslationValue = (QuaternionRotation) rotationTimeline.getValues()
					.get(floorIndex);
			rotationHeap.x = (float) oldTranslationValue.x;
			rotationHeap.y = (float) oldTranslationValue.y;
			rotationHeap.z = (float) oldTranslationValue.z;
			rotationHeap.w = (float) oldTranslationValue.w;
			QuaternionRotation.mul(localRotation, rotationHeap, rotationHeap);

			oldTranslationValue.x = rotationHeap.x;
			oldTranslationValue.y = rotationHeap.y;
			oldTranslationValue.z = rotationHeap.z;
			oldTranslationValue.w = rotationHeap.w;

			if (rotationTimeline.tans()) {
				final QuaternionRotation oldInTan = (QuaternionRotation) rotationTimeline.getInTans().get(floorIndex);
				rotationHeap.x = (float) oldInTan.x;
				rotationHeap.y = (float) oldInTan.y;
				rotationHeap.z = (float) oldInTan.z;
				rotationHeap.w = (float) oldInTan.w;
				QuaternionRotation.mul(localRotation, rotationHeap, rotationHeap);
				oldInTan.x = rotationHeap.x;
				oldInTan.y = rotationHeap.y;
				oldInTan.z = rotationHeap.z;
				oldInTan.w = rotationHeap.w;

				final QuaternionRotation oldOutTan = (QuaternionRotation) rotationTimeline.getOutTans().get(floorIndex);
				rotationHeap.x = (float) oldOutTan.x;
				rotationHeap.y = (float) oldOutTan.y;
				rotationHeap.z = (float) oldOutTan.z;
				rotationHeap.w = (float) oldOutTan.w;
				QuaternionRotation.mul(localRotation, rotationHeap, rotationHeap);
				oldOutTan.x = rotationHeap.x;
				oldOutTan.y = rotationHeap.y;
				oldOutTan.z = rotationHeap.z;
				oldOutTan.w = rotationHeap.w;
			}
		}
	}

	public void updateLocalRotationKeyframeInverse(final int trackTime, final Integer trackGlobalSeq,
			final QuaternionRotation localRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final AnimFlag rotationTimeline = find("Rotation", trackGlobalSeq);
		if (rotationTimeline == null) {
			return;
		}
		final int floorIndex = rotationTimeline.floorIndex(trackTime);

		if ((rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex) == trackTime)) {
			// we must change it
			final QuaternionRotation oldTranslationValue = (QuaternionRotation) rotationTimeline.getValues()
					.get(floorIndex);
			rotationHeap.x = (float) oldTranslationValue.x;
			rotationHeap.y = (float) oldTranslationValue.y;
			rotationHeap.z = (float) oldTranslationValue.z;
			rotationHeap.w = (float) oldTranslationValue.w;
			rotationDeltaHeap.setIdentity();
			QuaternionRotation.mulInverse(rotationDeltaHeap, localRotation, rotationDeltaHeap);
			QuaternionRotation.mul(rotationDeltaHeap, rotationHeap, rotationHeap);

			oldTranslationValue.x = rotationHeap.x;
			oldTranslationValue.y = rotationHeap.y;
			oldTranslationValue.z = rotationHeap.z;
			oldTranslationValue.w = rotationHeap.w;

			if (rotationTimeline.tans()) {
				final QuaternionRotation oldInTan = (QuaternionRotation) rotationTimeline.getInTans().get(floorIndex);
				rotationHeap.x = (float) oldInTan.x;
				rotationHeap.y = (float) oldInTan.y;
				rotationHeap.z = (float) oldInTan.z;
				rotationHeap.w = (float) oldInTan.w;
				rotationDeltaHeap.setIdentity();
				QuaternionRotation.mulInverse(rotationDeltaHeap, localRotation, rotationDeltaHeap);
				QuaternionRotation.mul(rotationDeltaHeap, rotationHeap, rotationHeap);
				oldInTan.x = rotationHeap.x;
				oldInTan.y = rotationHeap.y;
				oldInTan.z = rotationHeap.z;
				oldInTan.w = rotationHeap.w;

				final QuaternionRotation oldOutTan = (QuaternionRotation) rotationTimeline.getOutTans().get(floorIndex);
				rotationHeap.x = (float) oldOutTan.x;
				rotationHeap.y = (float) oldOutTan.y;
				rotationHeap.z = (float) oldOutTan.z;
				rotationHeap.w = (float) oldOutTan.w;
				rotationDeltaHeap.setIdentity();
				QuaternionRotation.mulInverse(rotationDeltaHeap, localRotation, rotationDeltaHeap);
				QuaternionRotation.mul(rotationDeltaHeap, rotationHeap, rotationHeap);
				oldOutTan.x = rotationHeap.x;
				oldOutTan.y = rotationHeap.y;
				oldOutTan.z = rotationHeap.z;
				oldOutTan.w = rotationHeap.w;
			}
		}
	}

	public void updateLocalTranslationKeyframe(final int trackTime, final Integer trackGlobalSeq,
			final double newDeltaX, final double newDeltaY, final double newDeltaZ) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final AnimFlag translationFlag = find("Translation", trackGlobalSeq);
		if (translationFlag == null) {
			return;
		}
		final int floorIndex = translationFlag.floorIndex(trackTime);

		if ((translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex) == trackTime)) {
			// we must change it
			final Vertex oldTranslationValue = (Vertex) translationFlag.getValues().get(floorIndex);
			oldTranslationValue.x += newDeltaX;
			oldTranslationValue.y += newDeltaY;
			oldTranslationValue.z += newDeltaZ;

			if (translationFlag.tans()) {
				final Vertex oldInTan = (Vertex) translationFlag.getInTans().get(floorIndex);
				oldInTan.x += newDeltaX;
				oldInTan.y += newDeltaY;
				oldInTan.z += newDeltaZ;

				final Vertex oldOutTan = (Vertex) translationFlag.getOutTans().get(floorIndex);
				oldOutTan.x += newDeltaX;
				oldOutTan.y += newDeltaY;
				oldOutTan.z += newDeltaZ;
			}
		}

	}

	public void updateLocalScalingKeyframe(final int trackTime, final Integer trackGlobalSeq,
			final Vertex localScaling) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final AnimFlag translationFlag = find("Scaling", trackGlobalSeq);
		if (translationFlag == null) {
			return;
		}
		final int floorIndex = translationFlag.floorIndex(trackTime);

		if ((translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex) == trackTime)) {
			// we must change it
			final Vertex oldTranslationValue = (Vertex) translationFlag.getValues().get(floorIndex);
			oldTranslationValue.x *= localScaling.x;
			oldTranslationValue.y *= localScaling.y;
			oldTranslationValue.z *= localScaling.z;

			if (translationFlag.tans()) {
				final Vertex oldInTan = (Vertex) translationFlag.getInTans().get(floorIndex);
				oldInTan.x *= localScaling.x;
				oldInTan.y *= localScaling.y;
				oldInTan.z *= localScaling.z;

				final Vertex oldOutTan = (Vertex) translationFlag.getOutTans().get(floorIndex);
				oldOutTan.x *= localScaling.x;
				oldOutTan.y *= localScaling.y;
				oldOutTan.z *= localScaling.z;
			}
		}

	}
}
