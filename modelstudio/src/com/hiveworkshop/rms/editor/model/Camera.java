package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.AnimationMap;
import com.hiveworkshop.rms.parsers.mdlx.MdlxCamera;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
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

	public Camera(MdlxCamera camera) {
		name = camera.name;
		position = new Vec3(camera.position);
		fieldOfView = camera.fieldOfView;
		farClip = camera.farClippingPlane;
		nearClip = camera.nearClippingPlane;
		targetPosition = new Vec3(camera.targetPosition);

		for (MdlxTimeline<?> timeline : camera.timelines) {
			if (timeline.name == AnimationMap.KTTR.getWar3id()) {
				targetNode.add(AnimFlag.createFromTimeline(timeline));
			} else {
				sourceNode.add(AnimFlag.createFromTimeline(timeline));
			}
		}
	}

	public MdlxCamera toMdlx() {
		MdlxCamera camera = new MdlxCamera();

		camera.name = getName();
		camera.position = getPosition().toFloatArray();
		camera.fieldOfView = (float) getFieldOfView();
		camera.farClippingPlane = (float) getFarClip();
		camera.nearClippingPlane = (float) getNearClip();
		camera.targetPosition = getTargetPosition().toFloatArray();

		sourceNode.timelinesToMdlx(camera);
		targetNode.timelinesToMdlx(camera);

		return camera;
	}

	public void setName(String text) {
		name = text;
	}

	@Override
	public String getName() {
		return name;
	}

	public Vec3 getPosition() {
		return position;
	}

	public void setPosition(Vec3 position) {
		this.position = position;
	}

	public double getFieldOfView() {
		return fieldOfView;
	}

	public void setFieldOfView(double fieldOfView) {
		this.fieldOfView = fieldOfView;
	}

	public double getFarClip() {
		return farClip;
	}

	public void setFarClip(double farClip) {
		this.farClip = farClip;
	}

	public double getNearClip() {
		return nearClip;
	}

	public void setNearClip(double nearClip) {
		this.nearClip = nearClip;
	}

	public Vec3 getTargetPosition() {
		return targetPosition;
	}

	public void setTargetPosition(Vec3 targetPosition) {
		this.targetPosition = targetPosition;
	}

	public float[] getBindPose() {
		return bindPose;
	}

	public void setBindPose(float[] bindPose) {
		this.bindPose = bindPose;
	}

	public SourceNode getSourceNode() {
		return sourceNode;
	}

	public TargetNode getTargetNode() {
		return targetNode;
	}

	public static class SourceNode extends AnimatedNode {
		private static Quat rotationHeap = new Quat(0, 0, 0, 1);

		private Camera parent;

		private SourceNode(Camera parent) {
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
		public float getRenderVisibility(TimeEnvironmentImpl animatedRenderEnvironment) {
			return 1;
		}

		@Override
		public Quat getRenderRotation(TimeEnvironmentImpl animatedRenderEnvironment) {
			AnimFlag<?> translationFlag = find("Rotation");
			if (translationFlag != null) {
				Object interpolated = translationFlag.interpolateAt(animatedRenderEnvironment);
				if (interpolated instanceof Float) {
					Float angle = (Float) interpolated;
					Vec3 targetTranslation = parent.targetNode.getRenderTranslation(animatedRenderEnvironment);
					Vec3 targetPosition = parent.targetPosition;
					Vec3 sourceTranslation = getRenderTranslation(animatedRenderEnvironment);
					Vec3 sourcePosition = parent.position;
					Vec3 axisHeap = new Vec3(targetPosition).add(targetTranslation).sub(sourcePosition).sub(sourceTranslation);
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

		public float getRenderRotationScalar(TimeEnvironmentImpl animatedRenderEnvironment) {
//			return getInterpolatedFloat(animatedRenderEnvironment, "Rotation", 0);
			return getInterpolatedInteger(animatedRenderEnvironment, MdlUtils.TOKEN_ROTATION, 0);
		}

		@Override
		public Vec3 getRenderScale(TimeEnvironmentImpl animatedRenderEnvironment) {
			return AnimFlag.SCALE_IDENTITY;
		}
	}

	public static class TargetNode extends AnimatedNode {
		private Camera parent;

		private TargetNode(Camera parent) {
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
		public float getRenderVisibility(TimeEnvironmentImpl animatedRenderEnvironment) {
			return 1;
		}

		@Override
		public Quat getRenderRotation(TimeEnvironmentImpl animatedRenderEnvironment) {
			return AnimFlag.ROTATE_IDENTITY;
		}

		@Override
		public Vec3 getRenderScale(TimeEnvironmentImpl animatedRenderEnvironment) {
			return AnimFlag.SCALE_IDENTITY;
		}
	}
}
