package com.hiveworkshop.rms.editor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hiveworkshop.rms.parsers.mdlx.AnimationMap;
import com.hiveworkshop.rms.parsers.mdlx.MdlxCamera;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.util.QuaternionRotation;
import com.hiveworkshop.rms.util.Vertex;

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
	Vertex position;
	double fieldOfView;
	double farClip;
	double nearClip;
	Vertex targetPosition;
	List<AnimFlag> targetAnimFlags = new ArrayList<>();
	final SourceNode sourceNode = new SourceNode(this);
	final TargetNode targetNode = new TargetNode(this);
	float[] bindPose;

	public Camera(final MdlxCamera camera) {
		name = camera.name;
		position = new Vertex(camera.position);
		fieldOfView = camera.fieldOfView;
		farClip = camera.farClippingPlane;
		nearClip = camera.nearClippingPlane;
		targetPosition = new Vertex(camera.targetPosition);

		for (final MdlxTimeline<?> timeline : camera.timelines) {
			if (timeline.name == AnimationMap.KTTR.getWar3id()) {
				targetNode.add(new AnimFlag(timeline));
			} else {
				sourceNode.add(new AnimFlag(timeline));
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

	public Vertex getPosition() {
		return position;
	}

	public void setPosition(final Vertex position) {
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

	public Vertex getTargetPosition() {
		return targetPosition;
	}

	public void setTargetPosition(final Vertex targetPosition) {
		this.targetPosition = targetPosition;
	}

	public SourceNode getSourceNode() {
		return sourceNode;
	}

	public TargetNode getTargetNode() {
		return targetNode;
	}

	public static final class SourceNode extends AnimatedNode {
		private static final QuaternionRotation rotationHeap = new QuaternionRotation(0, 0, 0, 1);
		
		private final Camera parent;
		private final Vertex axisHeap = new Vertex(0, 0, 0);

		private SourceNode(final Camera parent) {
			this.parent = parent;
		}

		@Override
		public AnimatedNode getParent() {
			return null;
		}

		@Override
		public Vertex getPivotPoint() {
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
		public QuaternionRotation getRenderRotation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			final AnimFlag translationFlag = find("Rotation");
			if (translationFlag != null) {
				final Object interpolated = translationFlag.interpolateAt(animatedRenderEnvironment);
				if (interpolated instanceof Double) {
					final Double angle = (Double) interpolated;
					final Vertex targetTranslation = parent.targetNode.getRenderTranslation(animatedRenderEnvironment);
					final Vertex targetPosition = parent.targetPosition;
					final Vertex sourceTranslation = getRenderTranslation(animatedRenderEnvironment);
					final Vertex sourcePosition = parent.position;
					axisHeap.x = (targetPosition.x + targetTranslation.x) - (sourcePosition.x + sourceTranslation.x);
					axisHeap.y = (targetPosition.y + targetTranslation.y) - (sourcePosition.y + sourceTranslation.y);
					axisHeap.z = (targetPosition.z + targetTranslation.z) - (sourcePosition.z + sourceTranslation.z);
					rotationHeap.setFromAxisAngle(axisHeap, angle.floatValue());
					return rotationHeap;
				} else {
					return (QuaternionRotation) interpolated;
				}
			}
			return null;
		}

		public float getRenderRotationScalar(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			return getInterpolatedFloat(animatedRenderEnvironment, "Rotation", 0);
		}

		@Override
		public Vertex getRenderScale(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			return AnimFlag.SCALE_IDENTITY;
		}
	}

	public static final class TargetNode extends AnimatedNode {
		private final Camera parent;

		private TargetNode(final Camera parent) {
			this.parent = parent;
		}

		@Override
		public AnimatedNode getParent() {
			return null;
		}

		@Override
		public Vertex getPivotPoint() {
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
		public QuaternionRotation getRenderRotation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
			return AnimFlag.ROTATE_IDENTITY;
		}

		@Override
		public Vertex getRenderScale(final AnimatedRenderEnvironment animatedRenderEnvironment) {
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
