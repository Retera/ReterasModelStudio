package com.hiveworkshop.rms.editor.model;

import java.util.List;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation.AddKeyframeAction;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vector3;
import com.hiveworkshop.rms.util.Vector4;

public abstract class AnimatedNode extends TimelineContainer {
	private static final Vector4 translationHeap = new Vector4();
	private static final Mat4 matrixHeap = new Mat4();
	private static final Quat rotationHeap = new Quat();
	private static final Quat rotationDeltaHeap = new Quat();
	private static final Vector4 axisAngleHeap = new Vector4();

	abstract public AnimatedNode getParent();

	abstract public Vector3 getPivotPoint();

	abstract public List<? extends AnimatedNode> getChildrenNodes();

	abstract public String getName();
	
	public Vector3 getRenderTranslation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedVector(animatedRenderEnvironment, "Translation", null);
	}

	public Quat getRenderRotation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedQuat(animatedRenderEnvironment, "Rotation", null);
	}

	public Vector3 getRenderScale(final AnimatedRenderEnvironment animatedRenderEnvironment) {
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
			final Vector3 localLocation = renderNode.getLocalLocation();
			final int insertIndex = ((translationFlag.getTimes().size() == 0)
					|| (translationFlag.getTimes().get(0) > trackTime)) ? 0 : floorIndex + 1;
			translationFlag.getTimes().add(insertIndex, trackTime);
			final Vector3 keyframeValue = new Vector3(localLocation.x, localLocation.y, localLocation.z);
			translationFlag.getValues().add(insertIndex, keyframeValue);
			if (translationFlag.tans()) {
				final Vector3 inTan = new Vector3(localLocation.x, localLocation.y, localLocation.z);
				translationFlag.getInTans().add(insertIndex, inTan);
				final Vector3 outTan = new Vector3(localLocation.x, localLocation.y, localLocation.z);
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
			final Quat localRotation = renderNode.getLocalRotation();
			final int insertIndex = ((rotationTimeline.getTimes().size() == 0)
					|| (rotationTimeline.getTimes().get(0) > trackTime)) ? 0 : floorIndex + 1;
			rotationTimeline.getTimes().add(insertIndex, trackTime);
			final Quat keyframeValue = new Quat(localRotation.x, localRotation.y,
					localRotation.z, localRotation.w);
			rotationTimeline.getValues().add(insertIndex, keyframeValue);
			if (rotationTimeline.tans()) {
				final Quat inTan = new Quat(localRotation.x, localRotation.y,
						localRotation.z, localRotation.w);
				rotationTimeline.getInTans().add(insertIndex, inTan);
				final Quat outTan = new Quat(localRotation.x, localRotation.y,
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
			final Vector3 localScale = renderNode.getLocalScale();
			final int insertIndex = ((scalingTimeline.getTimes().size() == 0)
					|| (scalingTimeline.getTimes().get(0) > trackTime)) ? 0 : floorIndex + 1;
			scalingTimeline.getTimes().add(insertIndex, trackTime);
			final Vector3 keyframeValue = new Vector3(localScale.x, localScale.y, localScale.z);
			scalingTimeline.getValues().add(insertIndex, keyframeValue);
			if (scalingTimeline.tans()) {
				final Vector3 inTan = new Vector3(localScale.x, localScale.y, localScale.z);
				scalingTimeline.getInTans().add(insertIndex, inTan);
				final Vector3 outTan = new Vector3(localScale.x, localScale.y, localScale.z);
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
			final double newDeltaZ, final Vector3 savedLocalTranslation) {
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
			parentRenderNode.getWorldMatrix().invert(matrixHeap);

			translationHeap.x = 0;
			translationHeap.y = 0;
			translationHeap.z = 0;
			translationHeap.w = 1;

			parentRenderNode.getWorldMatrix().transform(translationHeap);

			translationHeap.x = (float) (translationHeap.x + newDeltaX);
			translationHeap.y = (float) (translationHeap.y + newDeltaY);
			translationHeap.z = (float) (translationHeap.z + newDeltaZ);
			translationHeap.w = 1;

			matrixHeap.transform(translationHeap);
		} else {
			translationHeap.x = (float) (newDeltaX);
			translationHeap.y = (float) (newDeltaY);
			translationHeap.z = (float) (newDeltaZ);
			translationHeap.w = 1;
		}

		if ((translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex) == trackTime)) {
			// we must change it
			final Vector3 oldTranslationValue = (Vector3) translationFlag.getValues().get(floorIndex);
			oldTranslationValue.x += translationHeap.x;
			oldTranslationValue.y += translationHeap.y;
			oldTranslationValue.z += translationHeap.z;

			if (savedLocalTranslation != null) {
				savedLocalTranslation.x += translationHeap.x;
				savedLocalTranslation.y += translationHeap.y;
				savedLocalTranslation.z += translationHeap.z;
			}

			if (translationFlag.tans()) {
				final Vector3 oldInTan = (Vector3) translationFlag.getInTans().get(floorIndex);
				oldInTan.x += translationHeap.x;
				oldInTan.y += translationHeap.y;
				oldInTan.z += translationHeap.z;

				final Vector3 oldOutTan = (Vector3) translationFlag.getOutTans().get(floorIndex);
				oldOutTan.x += translationHeap.x;
				oldOutTan.y += translationHeap.y;
				oldOutTan.z += translationHeap.z;
			}
		}

	}

	public void updateRotationKeyframe(final RenderModel renderModel, final double centerX, final double centerY,
			final double centerZ, final double radians, final byte firstXYZ, final byte secondXYZ,
			final Quat savedLocalRotation) {
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
			parentRenderNode.getWorldMatrix().invert(matrixHeap);

			axisAngleHeap.x = 0;
			axisAngleHeap.y = 0;
			axisAngleHeap.z = 0;
			axisAngleHeap.w = 1;

			parentRenderNode.getWorldMatrix().transform(axisAngleHeap);

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

			matrixHeap.transform(axisAngleHeap);
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
			final Quat oldTranslationValue = (Quat) rotationTimeline.getValues()
					.get(floorIndex);
			rotationHeap.x = (float) oldTranslationValue.x;
			rotationHeap.y = (float) oldTranslationValue.y;
			rotationHeap.z = (float) oldTranslationValue.z;
			rotationHeap.w = (float) oldTranslationValue.w;
			rotationDeltaHeap.mul(rotationHeap, rotationHeap);

			oldTranslationValue.x = rotationHeap.x;
			oldTranslationValue.y = rotationHeap.y;
			oldTranslationValue.z = rotationHeap.z;
			oldTranslationValue.w = rotationHeap.w;

			if (savedLocalRotation != null) {
				savedLocalRotation.mul(rotationDeltaHeap);
			}

			if (rotationTimeline.tans()) {
				final Quat oldInTan = (Quat) rotationTimeline.getInTans().get(floorIndex);
				rotationHeap.x = (float) oldInTan.x;
				rotationHeap.y = (float) oldInTan.y;
				rotationHeap.z = (float) oldInTan.z;
				rotationHeap.w = (float) oldInTan.w;
				rotationDeltaHeap.mul(rotationHeap, rotationHeap);
				oldInTan.x = rotationHeap.x;
				oldInTan.y = rotationHeap.y;
				oldInTan.z = rotationHeap.z;
				oldInTan.w = rotationHeap.w;

				final Quat oldOutTan = (Quat) rotationTimeline.getOutTans().get(floorIndex);
				rotationHeap.x = (float) oldOutTan.x;
				rotationHeap.y = (float) oldOutTan.y;
				rotationHeap.z = (float) oldOutTan.z;
				rotationHeap.w = (float) oldOutTan.w;
				rotationDeltaHeap.mul(rotationHeap, rotationHeap);
				oldOutTan.x = rotationHeap.x;
				oldOutTan.y = rotationHeap.y;
				oldOutTan.z = rotationHeap.z;
				oldOutTan.w = rotationHeap.w;
			}
		}
	}

	public void updateScalingKeyframe(final RenderModel renderModel, final double scaleX, final double scaleY,
			final double scaleZ, final Vector3 savedLocalScaling) {
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
			final Vector3 oldTranslationValue = (Vector3) translationFlag.getValues().get(floorIndex);
			oldTranslationValue.x *= translationHeap.x;
			oldTranslationValue.y *= translationHeap.y;
			oldTranslationValue.z *= translationHeap.z;

			if (savedLocalScaling != null) {
				savedLocalScaling.x *= translationHeap.x;
				savedLocalScaling.y *= translationHeap.y;
				savedLocalScaling.z *= translationHeap.z;
			}

			if (translationFlag.tans()) {
				final Vector3 oldInTan = (Vector3) translationFlag.getInTans().get(floorIndex);
				oldInTan.x *= translationHeap.x;
				oldInTan.y *= translationHeap.y;
				oldInTan.z *= translationHeap.z;

				final Vector3 oldOutTan = (Vector3) translationFlag.getOutTans().get(floorIndex);
				oldOutTan.x *= translationHeap.x;
				oldOutTan.y *= translationHeap.y;
				oldOutTan.z *= translationHeap.z;
			}
		}
	}

	public void updateLocalRotationKeyframe(final int trackTime, final Integer trackGlobalSeq,
			final Quat localRotation) {
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
			final Quat oldTranslationValue = (Quat) rotationTimeline.getValues()
					.get(floorIndex);
			rotationHeap.x = (float) oldTranslationValue.x;
			rotationHeap.y = (float) oldTranslationValue.y;
			rotationHeap.z = (float) oldTranslationValue.z;
			rotationHeap.w = (float) oldTranslationValue.w;
			localRotation.mul(rotationHeap, rotationHeap);

			oldTranslationValue.x = rotationHeap.x;
			oldTranslationValue.y = rotationHeap.y;
			oldTranslationValue.z = rotationHeap.z;
			oldTranslationValue.w = rotationHeap.w;

			if (rotationTimeline.tans()) {
				final Quat oldInTan = (Quat) rotationTimeline.getInTans().get(floorIndex);
				rotationHeap.x = (float) oldInTan.x;
				rotationHeap.y = (float) oldInTan.y;
				rotationHeap.z = (float) oldInTan.z;
				rotationHeap.w = (float) oldInTan.w;
				localRotation.mul(rotationHeap, rotationHeap);
				oldInTan.x = rotationHeap.x;
				oldInTan.y = rotationHeap.y;
				oldInTan.z = rotationHeap.z;
				oldInTan.w = rotationHeap.w;

				final Quat oldOutTan = (Quat) rotationTimeline.getOutTans().get(floorIndex);
				rotationHeap.x = (float) oldOutTan.x;
				rotationHeap.y = (float) oldOutTan.y;
				rotationHeap.z = (float) oldOutTan.z;
				rotationHeap.w = (float) oldOutTan.w;
				localRotation.mul(rotationHeap, rotationHeap);
				oldOutTan.x = rotationHeap.x;
				oldOutTan.y = rotationHeap.y;
				oldOutTan.z = rotationHeap.z;
				oldOutTan.w = rotationHeap.w;
			}
		}
	}

	public void updateLocalRotationKeyframeInverse(final int trackTime, final Integer trackGlobalSeq,
			final Quat localRotation) {
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
			final Quat oldTranslationValue = (Quat) rotationTimeline.getValues()
					.get(floorIndex);
			rotationHeap.x = (float) oldTranslationValue.x;
			rotationHeap.y = (float) oldTranslationValue.y;
			rotationHeap.z = (float) oldTranslationValue.z;
			rotationHeap.w = (float) oldTranslationValue.w;
			rotationDeltaHeap.setIdentity();
			rotationDeltaHeap.mulInverse(localRotation);
			rotationDeltaHeap.mul(rotationHeap, rotationHeap);

			oldTranslationValue.x = rotationHeap.x;
			oldTranslationValue.y = rotationHeap.y;
			oldTranslationValue.z = rotationHeap.z;
			oldTranslationValue.w = rotationHeap.w;

			if (rotationTimeline.tans()) {
				final Quat oldInTan = (Quat) rotationTimeline.getInTans().get(floorIndex);
				rotationHeap.x = (float) oldInTan.x;
				rotationHeap.y = (float) oldInTan.y;
				rotationHeap.z = (float) oldInTan.z;
				rotationHeap.w = (float) oldInTan.w;
				rotationDeltaHeap.setIdentity();
				rotationDeltaHeap.mulInverse(localRotation);
				rotationDeltaHeap.mul(rotationHeap, rotationHeap);
				oldInTan.x = rotationHeap.x;
				oldInTan.y = rotationHeap.y;
				oldInTan.z = rotationHeap.z;
				oldInTan.w = rotationHeap.w;

				final Quat oldOutTan = (Quat) rotationTimeline.getOutTans().get(floorIndex);
				rotationHeap.x = (float) oldOutTan.x;
				rotationHeap.y = (float) oldOutTan.y;
				rotationHeap.z = (float) oldOutTan.z;
				rotationHeap.w = (float) oldOutTan.w;
				rotationDeltaHeap.setIdentity();
				rotationDeltaHeap.mulInverse(localRotation);
				rotationDeltaHeap.mul(rotationHeap, rotationHeap);
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
			final Vector3 oldTranslationValue = (Vector3) translationFlag.getValues().get(floorIndex);
			oldTranslationValue.x += newDeltaX;
			oldTranslationValue.y += newDeltaY;
			oldTranslationValue.z += newDeltaZ;

			if (translationFlag.tans()) {
				final Vector3 oldInTan = (Vector3) translationFlag.getInTans().get(floorIndex);
				oldInTan.x += newDeltaX;
				oldInTan.y += newDeltaY;
				oldInTan.z += newDeltaZ;

				final Vector3 oldOutTan = (Vector3) translationFlag.getOutTans().get(floorIndex);
				oldOutTan.x += newDeltaX;
				oldOutTan.y += newDeltaY;
				oldOutTan.z += newDeltaZ;
			}
		}

	}

	public void updateLocalScalingKeyframe(final int trackTime, final Integer trackGlobalSeq,
			final Vector3 localScaling) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final AnimFlag translationFlag = find("Scaling", trackGlobalSeq);
		if (translationFlag == null) {
			return;
		}
		final int floorIndex = translationFlag.floorIndex(trackTime);

		if ((translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex) == trackTime)) {
			// we must change it
			final Vector3 oldTranslationValue = (Vector3) translationFlag.getValues().get(floorIndex);
			oldTranslationValue.x *= localScaling.x;
			oldTranslationValue.y *= localScaling.y;
			oldTranslationValue.z *= localScaling.z;

			if (translationFlag.tans()) {
				final Vector3 oldInTan = (Vector3) translationFlag.getInTans().get(floorIndex);
				oldInTan.x *= localScaling.x;
				oldInTan.y *= localScaling.y;
				oldInTan.z *= localScaling.z;

				final Vector3 oldOutTan = (Vector3) translationFlag.getOutTans().get(floorIndex);
				oldOutTan.x *= localScaling.x;
				oldOutTan.y *= localScaling.y;
				oldOutTan.z *= localScaling.z;
			}
		}

	}
}
