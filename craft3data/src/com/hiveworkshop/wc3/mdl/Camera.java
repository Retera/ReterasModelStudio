package com.hiveworkshop.wc3.mdl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.etheller.warsmash.parsers.mdlx.AnimationMap;
import com.etheller.warsmash.parsers.mdlx.MdlxCamera;
import com.etheller.warsmash.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;

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
	Vertex Position;
	double FieldOfView;
	double FarClip;
	double NearClip;
	Vertex targetPosition;
	List<AnimFlag> targetAnimFlags = new ArrayList<>();
	final SourceNode sourceNode = new SourceNode(this);
	final TargetNode targetNode = new TargetNode(this);
	float[] bindPose;

	public SourceNode getSourceNode() {
		return sourceNode;
	}

	public TargetNode getTargetNode() {
		return targetNode;
	}

	public Camera(final MdlxCamera camera) {
		name = camera.name;
		Position = new Vertex(camera.position);
		FieldOfView = camera.fieldOfView;
		FarClip = camera.farClippingPlane;
		NearClip = camera.nearClippingPlane;
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
		MdlxCamera camera = new MdlxCamera();

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
		return Position;
	}

	public void setPosition(final Vertex position) {
		Position = position;
	}

	public double getFieldOfView() {
		return FieldOfView;
	}

	public void setFieldOfView(final double fieldOfView) {
		FieldOfView = fieldOfView;
	}

	public double getFarClip() {
		return FarClip;
	}

	public void setFarClip(final double farClip) {
		FarClip = farClip;
	}

	public double getNearClip() {
		return NearClip;
	}

	public void setNearClip(final double nearClip) {
		NearClip = nearClip;
	}

	public Vertex getTargetPosition() {
		return targetPosition;
	}

	public void setTargetPosition(final Vertex targetPosition) {
		this.targetPosition = targetPosition;
	}

	public static final class SourceNode extends AnimatedNode {
		private final Camera parent;
		private static final QuaternionRotation rotationHeap = new QuaternionRotation(0, 0, 0, 1);
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
			return parent.Position;
		}

		@Override
		public List<? extends AnimatedNode> getChildrenNodes() {
			return Collections.EMPTY_LIST;
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
					final Vertex sourcePosition = parent.Position;
					axisHeap.x = (targetPosition.x + targetTranslation.x) - (sourcePosition.x + sourceTranslation.x);
					axisHeap.y = (targetPosition.y + targetTranslation.y) - (sourcePosition.y + sourceTranslation.y);
					axisHeap.z = (targetPosition.z + targetTranslation.z) - (sourcePosition.z + sourceTranslation.z);
					rotationHeap.set(axisHeap, angle);
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
			return Collections.EMPTY_LIST;
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
