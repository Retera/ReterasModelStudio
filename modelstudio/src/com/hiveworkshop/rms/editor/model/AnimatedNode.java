package com.hiveworkshop.rms.editor.model;

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
	private static final Vec4 translationHeap = new Vec4();
	private static final Mat4 matrixHeap = new Mat4();
	private static final Quat rotationHeap = new Quat();
	private static final Quat rotationDeltaHeap = new Quat();
	private static final Vec4 axisAngleHeap = new Vec4();

	abstract public AnimatedNode getParent();

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

	public AddKeyframeAction createTranslationKeyframe(final RenderModel renderModel, final AnimFlag translationFlag,
                                                       final ModelStructureChangeListener structureChangeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		int trackTime = getTrackTime(renderModel);
		final int floorIndex = translationFlag.floorIndex(trackTime);
		final RenderNode renderNode = renderModel.getRenderNode(this);

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex) == trackTime)) {
			return null;
		} else {
			final Vec3 localLocation = renderNode.getLocalLocation();
			return getAddKeyframeAction(translationFlag, structureChangeListener, trackTime, floorIndex, localLocation);
		}
	}

	public AddKeyframeAction createRotationKeyframe(final RenderModel renderModel, final AnimFlag rotationTimeline,
	                                                final ModelStructureChangeListener structureChangeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		int trackTime = getTrackTime(renderModel);
		final int floorIndex = rotationTimeline.floorIndex(trackTime);
		final RenderNode renderNode = renderModel.getRenderNode(this);

		if ((floorIndex != -1) && (rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex) == trackTime)) {
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

	public AddKeyframeAction createScalingKeyframe(final RenderModel renderModel, final AnimFlag scalingTimeline,
	                                               final ModelStructureChangeListener structureChangeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		int trackTime = getTrackTime(renderModel);
		final int floorIndex = scalingTimeline.floorIndex(trackTime);
		final RenderNode renderNode = renderModel.getRenderNode(this);

		if ((floorIndex != -1) && (scalingTimeline.getTimes().size() > 0) && (scalingTimeline.getTimes().get(floorIndex) == trackTime)) {
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

	private AddKeyframeAction getAddKeyframeAction(AnimFlag timeline, ModelStructureChangeListener structureChangeListener, int trackTime, int floorIndex, Vec3 vec3) {
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

	public void updateTranslationKeyframe(final RenderModel renderModel, final double newDeltaX, final double newDeltaY,
	                                      final double newDeltaZ, final Vec3 savedLocalTranslation) {
		// Note to future author: the reason for saved local translation is that
		// we would like to be able to undo the action of moving the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must  make AnimFlag.find seek on globalSeqId
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from
		//  a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();
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

			setXYZ(translationHeap, 0, 0, 0);
			translationHeap.w = 1;

			parentRenderNode.getWorldMatrix().transform(translationHeap);

			setFloat(translationHeap.x + newDeltaX, translationHeap.y + newDeltaY, translationHeap.z + newDeltaZ);

			matrixHeap.transform(translationHeap);
		} else {
			setFloat(newDeltaX, newDeltaY, newDeltaZ);
		}

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex) == trackTime)) {
			// we must change it
			final Vec3 oldTranslationValue = (Vec3) translationFlag.getValues().get(floorIndex);
			addTo(oldTranslationValue, translationHeap.x, translationHeap.y, translationHeap.z);

			if (savedLocalTranslation != null) {
				addTo(savedLocalTranslation, translationHeap.x, translationHeap.y, translationHeap.z);
			}

			if (translationFlag.tans()) {
				final Vec3 oldInTan = (Vec3) translationFlag.getInTans().get(floorIndex);
				addTo(oldInTan, translationHeap.x, translationHeap.y, translationHeap.z);

				final Vec3 oldOutTan = (Vec3) translationFlag.getOutTans().get(floorIndex);
				addTo(oldOutTan, translationHeap.x, translationHeap.y, translationHeap.z);
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

			setXYZ(axisAngleHeap, 0, 0, 0);
			axisAngleHeap.w = 1;

			parentRenderNode.getWorldMatrix().transform(axisAngleHeap);

			switch (unusedXYZ) {
				case 0 -> setXYZ(axisAngleHeap, (axisAngleHeap.x + 1), axisAngleHeap.y + 0, axisAngleHeap.z + 0);
				case 1 -> setXYZ(axisAngleHeap, (axisAngleHeap.x + 0), axisAngleHeap.y + -1, axisAngleHeap.z + 0);
				case 2 -> setXYZ(axisAngleHeap, (axisAngleHeap.x + 0), axisAngleHeap.y + 0, axisAngleHeap.z + -1);
			}
			axisAngleHeap.w = 1;

			matrixHeap.transform(axisAngleHeap);
		} else {
			switch (unusedXYZ) {
				case 0 -> setXYZ(axisAngleHeap, 1, 0, 0);
				case 1 -> setXYZ(axisAngleHeap, 0, -1, 0);
				case 2 -> setXYZ(axisAngleHeap, 0, 0, -1);
			}
		}
		axisAngleHeap.w = (float) radians;
		rotationDeltaHeap.setFromAxisAngle(axisAngleHeap);

		if ((floorIndex != -1) && (rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex) == trackTime)) {
			// we must change it
			final Quat oldTranslationValue = (Quat) rotationTimeline.getValues()
					.get(floorIndex);
			setStuff(oldTranslationValue, rotationDeltaHeap);

			if (savedLocalRotation != null) {
				savedLocalRotation.mul(rotationDeltaHeap);
			}

			if (rotationTimeline.tans()) {
				final Quat oldInTan = (Quat) rotationTimeline.getInTans().get(floorIndex);
				setStuff(oldInTan, rotationDeltaHeap);

				final Quat oldOutTan = (Quat) rotationTimeline.getOutTans().get(floorIndex);
				setStuff(oldOutTan, rotationDeltaHeap);
			}
		}
	}

	public void updateScalingKeyframe(final RenderModel renderModel, final double scaleX, final double scaleY,
			final double scaleZ, final Vec3 savedLocalScaling) {
		// Note to future author: the reason for saved local scaling is that
		// we would like to be able to undo the action of moving the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from
		//  a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();
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

		setXYZ(translationHeap, (float) scaleX, (float) scaleY, (float) scaleZ);

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex) == trackTime)) {
			// we must change it
			final Vec3 oldTranslationValue = (Vec3) translationFlag.getValues().get(floorIndex);
			scaleBy(oldTranslationValue, translationHeap.x, translationHeap.y, translationHeap.z);

			if (savedLocalScaling != null) {
				scaleBy(savedLocalScaling, translationHeap.x, translationHeap.y, translationHeap.z);
			}

			if (translationFlag.tans()) {
				final Vec3 oldInTan = (Vec3) translationFlag.getInTans().get(floorIndex);
				scaleBy(oldInTan, translationHeap.x, translationHeap.y, translationHeap.z);

				final Vec3 oldOutTan = (Vec3) translationFlag.getOutTans().get(floorIndex);
				scaleBy(oldOutTan, translationHeap.x, translationHeap.y, translationHeap.z);
			}
		}
	}

	public void updateLocalRotationKeyframe(final int trackTime, final Integer trackGlobalSeq, final Quat localRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		final AnimFlag rotationTimeline = find("Rotation", trackGlobalSeq);
		if (rotationTimeline == null) {
			return;
		}
		final int floorIndex = rotationTimeline.floorIndex(trackTime);

		if ((floorIndex != -1) && (rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex) == trackTime)) {
			// we must change it
			final Quat oldTranslationValue = (Quat) rotationTimeline.getValues().get(floorIndex);
			setStuff(oldTranslationValue, localRotation);

			if (rotationTimeline.tans()) {
				final Quat oldInTan = (Quat) rotationTimeline.getInTans().get(floorIndex);
				setStuff(oldInTan, localRotation);

				final Quat oldOutTan = (Quat) rotationTimeline.getOutTans().get(floorIndex);
				setStuff(oldOutTan, localRotation);
			}
		}
	}

	public void updateLocalRotationKeyframeInverse(final int trackTime, final Integer trackGlobalSeq, final Quat localRotation) {
		// Note to future author: the reason for saved local rotation is that
		// we would like to be able to undo the action of rotating the animation data

		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		final AnimFlag rotationTimeline = find("Rotation", trackGlobalSeq);
		if (rotationTimeline == null) {
			return;
		}
		final int floorIndex = rotationTimeline.floorIndex(trackTime);

		if ((floorIndex != -1) && (rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex) == trackTime)) {
			// we must change it
			final Quat oldTranslationValue = (Quat) rotationTimeline.getValues().get(floorIndex);
			rotateStuff(localRotation, oldTranslationValue);

			if (rotationTimeline.tans()) {
				final Quat oldInTan = (Quat) rotationTimeline.getInTans().get(floorIndex);
				rotateStuff(localRotation, oldInTan);

				final Quat oldOutTan = (Quat) rotationTimeline.getOutTans().get(floorIndex);
				rotateStuff(localRotation, oldOutTan);
			}
		}
	}

	public void updateLocalTranslationKeyframe(final int trackTime, final Integer trackGlobalSeq,
			final double newDeltaX, final double newDeltaY, final double newDeltaZ) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		final AnimFlag translationFlag = find("Translation", trackGlobalSeq);
		if (translationFlag == null) {
			return;
		}
		final int floorIndex = translationFlag.floorIndex(trackTime);

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex) == trackTime)) {
			// we must change it
			final Vec3 oldTranslationValue = (Vec3) translationFlag.getValues().get(floorIndex);
			addTo(oldTranslationValue, newDeltaX, newDeltaY, newDeltaZ);

			if (translationFlag.tans()) {
				final Vec3 oldInTan = (Vec3) translationFlag.getInTans().get(floorIndex);
				addTo(oldInTan, newDeltaX, newDeltaY, newDeltaZ);

				final Vec3 oldOutTan = (Vec3) translationFlag.getOutTans().get(floorIndex);
				addTo(oldOutTan, newDeltaX, newDeltaY, newDeltaZ);
			}
		}

	}

	public void updateLocalScalingKeyframe(final int trackTime, final Integer trackGlobalSeq,
			final Vec3 localScaling) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		final AnimFlag translationFlag = find("Scaling", trackGlobalSeq);
		if (translationFlag == null) {
			return;
		}
		final int floorIndex = translationFlag.floorIndex(trackTime);

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex) == trackTime)) {
			// we must change it
			final Vec3 oldTranslationValue = (Vec3) translationFlag.getValues().get(floorIndex);
			scaleBy(oldTranslationValue, localScaling.x, localScaling.y, localScaling.z);

			if (translationFlag.tans()) {
				final Vec3 oldInTan = (Vec3) translationFlag.getInTans().get(floorIndex);
				scaleBy(oldInTan, localScaling.x, localScaling.y, localScaling.z);

				final Vec3 oldOutTan = (Vec3) translationFlag.getOutTans().get(floorIndex);
				scaleBy(oldOutTan, localScaling.x, localScaling.y, localScaling.z);
			}
		}

	}


	private void setFloat(double newDeltaX, double newDeltaY, double newDeltaZ) {
		setXYZ(translationHeap, (float) (newDeltaX), (float) (newDeltaY), (float) (newDeltaZ));
		translationHeap.w = 1;
	}

	private void addTo(Vec3 vec3Value, double x, double y, double z) {
		vec3Value.x += x;
		vec3Value.y += y;
		vec3Value.z += z;
	}

	private void scaleBy(Vec3 vec3Value, float x, float y, float z) {
		vec3Value.x *= x;
		vec3Value.y *= y;
		vec3Value.z *= z;
	}

	private void setXYZ(Vec4 vecHeap, float x, float y, float z) {
		vecHeap.x = x;
		vecHeap.y = y;
		vecHeap.z = z;
	}

	private void setStuff(Quat oldTan, Quat rotation) {
		setXYZ(rotationHeap, oldTan.x, oldTan.y, oldTan.z);
		rotationHeap.w = oldTan.w;

		rotation.mul(rotationHeap, rotationHeap);

		setXYZ(oldTan, rotationHeap.x, rotationHeap.y, rotationHeap.z);
		oldTan.w = rotationHeap.w;
	}

	private void rotateStuff(Quat localRotation, Quat oldInTan) {
		setXYZ(rotationHeap, oldInTan.x, oldInTan.y, oldInTan.z);
		rotationHeap.w = oldInTan.w;

		rotationDeltaHeap.setIdentity();
		rotationDeltaHeap.mulInverse(localRotation);
		rotationDeltaHeap.mul(rotationHeap, rotationHeap);

		setXYZ(oldInTan, rotationHeap.x, rotationHeap.y, rotationHeap.z);
		oldInTan.w = rotationHeap.w;
	}
}
