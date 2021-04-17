package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.AnimationMap;
import com.hiveworkshop.rms.parsers.mdlx.MdlxCamera;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Camera class, these are the things most people would think of as a particle
 * emitter, I think. Blizzard favored use of these over ParticleEmitters and I
 * do too simply because I so often recycle data and there are more of these to
 * use.
 *
 * Eric Theller 3/10/2012 3:32 PM
 */
public class Camera implements Named {
	String name;
	Vec3 position;
	double fieldOfView;
	double farClip;
	double nearClip;
	Vec3 targetPosition;
	List<AnimFlag<Vec3>> targetAnimFlags = new ArrayList<>();
	final SourceNode sourceNode = new SourceNode(this);
	final TargetNode targetNode = new TargetNode(this);
	float[] bindPose;

	public Camera(final MdlxCamera camera) {
		name = camera.name;
		position = new Vec3(camera.position);
		fieldOfView = camera.fieldOfView;
		farClip = camera.farClippingPlane;
		nearClip = camera.nearClippingPlane;
		targetPosition = new Vec3(camera.targetPosition);

		for (final MdlxTimeline<?> timeline : camera.timelines) {
			if (timeline.name == AnimationMap.KTTR.getWar3id()) {
				targetNode.add(AnimFlag.createFromTimeline(timeline));
			} else {
				sourceNode.add(AnimFlag.createFromTimeline(timeline));
			}
		}
	}

	public MdlxCamera toMdlx() {
		final MdlxCamera camera = new MdlxCamera();

		camera.name = getName();
		camera.position = getPosition().toFloatArray();
		camera.fieldOfView = (float)getFieldOfView();
		camera.farClippingPlane = (float)getFarClip();
		camera.nearClippingPlane = (float)getNearClip();
		camera.targetPosition = getTargetPosition().toFloatArray();

		sourceNode.timelinesToMdlx(camera);
		targetNode.timelinesToMdlx(camera);

		return camera;
	}

	public void setName(final String text) {
		name = text;
	}

	@Override
	public String getName() {
		return name;
	}

	public Vec3 getPosition() {
		return position;
	}

	public void setPosition(final Vec3 position) {
		this.position = position;
	}

	public double getFieldOfView() {
		return fieldOfView;
	}

	public void setFieldOfView(final double fieldOfView) {
		this.fieldOfView = fieldOfView;
	}

	public double getFarClip() {
		return farClip;
	}

	public void setFarClip(final double farClip) {
		this.farClip = farClip;
	}

	public double getNearClip() {
		return nearClip;
	}

	public void setNearClip(final double nearClip) {
		this.nearClip = nearClip;
	}

	public Vec3 getTargetPosition() {
		return targetPosition;
	}

	public void setTargetPosition(final Vec3 targetPosition) {
		this.targetPosition = targetPosition;
	}

	public SourceNode getSourceNode() {
		return sourceNode;
	}

	public TargetNode getTargetNode() {
		return targetNode;
	}

	public static final class SourceNode extends AnimatedNode {
		private static final Quat rotationHeap = new Quat(0, 0, 0, 1);

		private final Camera parent;

		private SourceNode(final Camera parent) {
			this.parent = parent;
		}

//		@Override
//		public AnimatedNode getParent() {
//			return null;
//		}

		@Override
		public Vec3 getPivotPoint() {
			return parent.position;
		}

		@Override
		public List<? extends AnimatedNode> getChildrenNodes() {
			return Collections.emptyList();
		}

		@Override
		public String getName() {
			return "Source of: " + parent.name;
		}

		@Override
		public float getRenderVisibility(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			return 1;
		}

		@Override
		public Quat getRenderRotation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			final AnimFlag<?> translationFlag = find("Rotation");
			if (translationFlag != null) {
				final Object interpolated = translationFlag.interpolateAt(animatedRenderEnvironment);
				if (interpolated instanceof Float) {
					final Float angle = (Float) interpolated;
					final Vec3 targetTranslation = parent.targetNode.getRenderTranslation(animatedRenderEnvironment);
					final Vec3 targetPosition = parent.targetPosition;
					final Vec3 sourceTranslation = getRenderTranslation(animatedRenderEnvironment);
					final Vec3 sourcePosition = parent.position;
					final Vec3 axisHeap = new Vec3(targetPosition).add(targetTranslation).sub(sourcePosition).sub(sourceTranslation);
//					axisHeap.x = (targetPosition.x + targetTranslation.x) - (sourcePosition.x + sourceTranslation.x);
//					axisHeap.y = (targetPosition.y + targetTranslation.y) - (sourcePosition.y + sourceTranslation.y);
//					axisHeap.z = (targetPosition.z + targetTranslation.z) - (sourcePosition.z + sourceTranslation.z);
					rotationHeap.setFromAxisAngle(axisHeap, angle);
					return rotationHeap;
				} else {
					return (Quat) interpolated;
				}
			}
			return null;
		}

		public float getRenderRotationScalar(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			return getInterpolatedFloat(animatedRenderEnvironment, "Rotation", 0);
		}

		@Override
		public Vec3 getRenderScale(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			return AnimFlag.SCALE_IDENTITY;
		}
	}

	public static final class TargetNode extends AnimatedNode {
		private final Camera parent;

		private TargetNode(final Camera parent) {
			this.parent = parent;
		}

//		@Override
//		public AnimatedNode getParent() {
//			return null;
//		}

		@Override
		public Vec3 getPivotPoint() {
			return parent.targetPosition;
		}

		@Override
		public List<? extends AnimatedNode> getChildrenNodes() {
			return Collections.emptyList();
		}

		@Override
		public String getName() {
			return "Target of: " + parent.name;
		}

		@Override
		public float getRenderVisibility(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			return 1;
		}

		@Override
		public Quat getRenderRotation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			return AnimFlag.ROTATE_IDENTITY;
		}

		@Override
		public Vec3 getRenderScale(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			return AnimFlag.SCALE_IDENTITY;
		}
	}

	public void setBindPose(final float[] bindPose) {
		this.bindPose = bindPose;
	}

	public float[] getBindPose() {
		return bindPose;
	}
}
