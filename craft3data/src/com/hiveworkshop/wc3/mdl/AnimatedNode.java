package com.hiveworkshop.wc3.mdl;

import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.hiveworkshop.wc3.gui.animedit.TimeEnvironmentImpl;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.AddKeyframeAction;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.render3d.RenderModel;
import com.hiveworkshop.wc3.mdl.render3d.RenderNode;

public abstract class AnimatedNode extends TimelineContainer {
	private static final Vector4f translationHeap = new Vector4f();
	private static final Matrix4f matrixHeap = new Matrix4f();
	private static final Quaternion rotationHeap = new Quaternion();
	private static final Quaternion rotationDeltaHeap = new Quaternion();
	private static final Vector4f axisAngleHeap = new Vector4f();

	private static final Vector3f IDENTITY = new Vector3f(0, 0, 0);

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
			final Vector3f localLocation = renderNode.getLocalLocation();
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
			final Quaternion localRotation = renderNode.getLocalRotation();
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
			final Vector3f localScale = renderNode.getLocalScale();
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
			final double newDeltaZ, final Vector3f savedLocalTranslation) {
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
		final AnimFlag translationFlag = AnimFlag.find(getAnimFlags(), "Translation",
				timeEnvironmentImpl.getGlobalSeq());
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
			Matrix4f.invert(parentRenderNode.getWorldMatrix(), matrixHeap);

			translationHeap.x = 0;
			translationHeap.y = 0;
			translationHeap.z = 0;
			translationHeap.w = 1;

			Matrix4f.transform(parentRenderNode.getWorldMatrix(), translationHeap, translationHeap);

			translationHeap.x = (float) (translationHeap.x + newDeltaX);
			translationHeap.y = (float) (translationHeap.y + newDeltaY);
			translationHeap.z = (float) (translationHeap.z + newDeltaZ);
			translationHeap.w = 1;

			Matrix4f.transform(matrixHeap, translationHeap, translationHeap);
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
			final Quaternion savedLocalRotation) {
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
		final AnimFlag rotationTimeline = AnimFlag.find(getAnimFlags(), "Rotation", timeEnvironmentImpl.getGlobalSeq());
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
			Matrix4f.invert(parentRenderNode.getWorldMatrix(), matrixHeap);

			axisAngleHeap.x = 0;
			axisAngleHeap.y = 0;
			axisAngleHeap.z = 0;
			axisAngleHeap.w = 1;

			Matrix4f.transform(parentRenderNode.getWorldMatrix(), axisAngleHeap, axisAngleHeap);

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

			Matrix4f.transform(matrixHeap, axisAngleHeap, axisAngleHeap);
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
			rotationHeap.x = (float) oldTranslationValue.a;
			rotationHeap.y = (float) oldTranslationValue.b;
			rotationHeap.z = (float) oldTranslationValue.c;
			rotationHeap.w = (float) oldTranslationValue.d;
			Quaternion.mul(rotationDeltaHeap, rotationHeap, rotationHeap);

			oldTranslationValue.a = rotationHeap.x;
			oldTranslationValue.b = rotationHeap.y;
			oldTranslationValue.c = rotationHeap.z;
			oldTranslationValue.d = rotationHeap.w;

			if (savedLocalRotation != null) {
				Quaternion.mul(savedLocalRotation, rotationDeltaHeap, savedLocalRotation);
			}

			if (rotationTimeline.tans()) {
				final QuaternionRotation oldInTan = (QuaternionRotation) rotationTimeline.getInTans().get(floorIndex);
				rotationHeap.x = (float) oldInTan.a;
				rotationHeap.y = (float) oldInTan.b;
				rotationHeap.z = (float) oldInTan.c;
				rotationHeap.w = (float) oldInTan.d;
				Quaternion.mul(rotationDeltaHeap, rotationHeap, rotationHeap);
				oldInTan.a = rotationHeap.x;
				oldInTan.b = rotationHeap.y;
				oldInTan.c = rotationHeap.z;
				oldInTan.d = rotationHeap.w;

				final QuaternionRotation oldOutTan = (QuaternionRotation) rotationTimeline.getOutTans().get(floorIndex);
				rotationHeap.x = (float) oldOutTan.a;
				rotationHeap.y = (float) oldOutTan.b;
				rotationHeap.z = (float) oldOutTan.c;
				rotationHeap.w = (float) oldOutTan.d;
				Quaternion.mul(rotationDeltaHeap, rotationHeap, rotationHeap);
				oldOutTan.a = rotationHeap.x;
				oldOutTan.b = rotationHeap.y;
				oldOutTan.c = rotationHeap.z;
				oldOutTan.d = rotationHeap.w;
			}
		}
	}

	public void updateScalingKeyframe(final RenderModel renderModel, final double scaleX, final double scaleY,
			final double scaleZ, final Vector3f savedLocalScaling) {
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
		final AnimFlag translationFlag = AnimFlag.find(getAnimFlags(), "Scaling", timeEnvironmentImpl.getGlobalSeq());
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
			final Quaternion localRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final AnimFlag rotationTimeline = AnimFlag.find(getAnimFlags(), "Rotation", trackGlobalSeq);
		if (rotationTimeline == null) {
			return;
		}
		final int floorIndex = rotationTimeline.floorIndex(trackTime);

		if ((rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex) == trackTime)) {
			// we must change it
			final QuaternionRotation oldTranslationValue = (QuaternionRotation) rotationTimeline.getValues()
					.get(floorIndex);
			rotationHeap.x = (float) oldTranslationValue.a;
			rotationHeap.y = (float) oldTranslationValue.b;
			rotationHeap.z = (float) oldTranslationValue.c;
			rotationHeap.w = (float) oldTranslationValue.d;
			Quaternion.mul(localRotation, rotationHeap, rotationHeap);

			oldTranslationValue.a = rotationHeap.x;
			oldTranslationValue.b = rotationHeap.y;
			oldTranslationValue.c = rotationHeap.z;
			oldTranslationValue.d = rotationHeap.w;

			if (rotationTimeline.tans()) {
				final QuaternionRotation oldInTan = (QuaternionRotation) rotationTimeline.getInTans().get(floorIndex);
				rotationHeap.x = (float) oldInTan.a;
				rotationHeap.y = (float) oldInTan.b;
				rotationHeap.z = (float) oldInTan.c;
				rotationHeap.w = (float) oldInTan.d;
				Quaternion.mul(localRotation, rotationHeap, rotationHeap);
				oldInTan.a = rotationHeap.x;
				oldInTan.b = rotationHeap.y;
				oldInTan.c = rotationHeap.z;
				oldInTan.d = rotationHeap.w;

				final QuaternionRotation oldOutTan = (QuaternionRotation) rotationTimeline.getOutTans().get(floorIndex);
				rotationHeap.x = (float) oldOutTan.a;
				rotationHeap.y = (float) oldOutTan.b;
				rotationHeap.z = (float) oldOutTan.c;
				rotationHeap.w = (float) oldOutTan.d;
				Quaternion.mul(localRotation, rotationHeap, rotationHeap);
				oldOutTan.a = rotationHeap.x;
				oldOutTan.b = rotationHeap.y;
				oldOutTan.c = rotationHeap.z;
				oldOutTan.d = rotationHeap.w;
			}
		}
	}

	public void updateLocalRotationKeyframeInverse(final int trackTime, final Integer trackGlobalSeq,
			final Quaternion localRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final AnimFlag rotationTimeline = AnimFlag.find(getAnimFlags(), "Rotation", trackGlobalSeq);
		if (rotationTimeline == null) {
			return;
		}
		final int floorIndex = rotationTimeline.floorIndex(trackTime);

		if ((rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex) == trackTime)) {
			// we must change it
			final QuaternionRotation oldTranslationValue = (QuaternionRotation) rotationTimeline.getValues()
					.get(floorIndex);
			rotationHeap.x = (float) oldTranslationValue.a;
			rotationHeap.y = (float) oldTranslationValue.b;
			rotationHeap.z = (float) oldTranslationValue.c;
			rotationHeap.w = (float) oldTranslationValue.d;
			rotationDeltaHeap.setIdentity();
			Quaternion.mulInverse(rotationDeltaHeap, localRotation, rotationDeltaHeap);
			Quaternion.mul(rotationDeltaHeap, rotationHeap, rotationHeap);

			oldTranslationValue.a = rotationHeap.x;
			oldTranslationValue.b = rotationHeap.y;
			oldTranslationValue.c = rotationHeap.z;
			oldTranslationValue.d = rotationHeap.w;

			if (rotationTimeline.tans()) {
				final QuaternionRotation oldInTan = (QuaternionRotation) rotationTimeline.getInTans().get(floorIndex);
				rotationHeap.x = (float) oldInTan.a;
				rotationHeap.y = (float) oldInTan.b;
				rotationHeap.z = (float) oldInTan.c;
				rotationHeap.w = (float) oldInTan.d;
				rotationDeltaHeap.setIdentity();
				Quaternion.mulInverse(rotationDeltaHeap, localRotation, rotationDeltaHeap);
				Quaternion.mul(rotationDeltaHeap, rotationHeap, rotationHeap);
				oldInTan.a = rotationHeap.x;
				oldInTan.b = rotationHeap.y;
				oldInTan.c = rotationHeap.z;
				oldInTan.d = rotationHeap.w;

				final QuaternionRotation oldOutTan = (QuaternionRotation) rotationTimeline.getOutTans().get(floorIndex);
				rotationHeap.x = (float) oldOutTan.a;
				rotationHeap.y = (float) oldOutTan.b;
				rotationHeap.z = (float) oldOutTan.c;
				rotationHeap.w = (float) oldOutTan.d;
				rotationDeltaHeap.setIdentity();
				Quaternion.mulInverse(rotationDeltaHeap, localRotation, rotationDeltaHeap);
				Quaternion.mul(rotationDeltaHeap, rotationHeap, rotationHeap);
				oldOutTan.a = rotationHeap.x;
				oldOutTan.b = rotationHeap.y;
				oldOutTan.c = rotationHeap.z;
				oldOutTan.d = rotationHeap.w;
			}
		}
	}

	public void updateLocalTranslationKeyframe(final int trackTime, final Integer trackGlobalSeq,
			final double newDeltaX, final double newDeltaY, final double newDeltaZ) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final AnimFlag translationFlag = AnimFlag.find(getAnimFlags(), "Translation", trackGlobalSeq);
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
			final Vector3f localScaling) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must
		// make AnimFlag.find seek on globalSeqId
		final AnimFlag translationFlag = AnimFlag.find(getAnimFlags(), "Scaling", trackGlobalSeq);
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

	abstract public boolean hasFlag(IdObject.NodeFlags flag);

	abstract public AnimatedNode getParent();

	abstract public Vertex getPivotPoint();

	abstract public List<? extends AnimatedNode> getChildrenNodes();

	abstract public String getName();

	abstract public Vertex getRenderTranslation(AnimatedRenderEnvironment animatedRenderEnvironment);

	abstract public QuaternionRotation getRenderRotation(AnimatedRenderEnvironment animatedRenderEnvironment);

	abstract public Vertex getRenderScale(AnimatedRenderEnvironment animatedRenderEnvironment);
}
