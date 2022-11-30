package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.IntAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.MdlxCamera;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

/**
 * Camera class, these are the things most people would think of as a particle
 * emitter, I think. Blizzard favored use of these over ParticleEmitters and I
 * do too simply because I so often recycle data and there are more of these to
 * use.
 *
 * Eric Theller 3/10/2012 3:32 PM
 */
public abstract class CameraNode extends AnimatedNode {
	protected final Camera parent;
	protected final Vec3 position;
	protected static final Quat rotationHeap = new Quat(0, 0, 0, 1);
	Vec3 axisHeap = new Vec3();

	private CameraNode(Camera camera) {
		this(camera, new Vec3());
	}

	private CameraNode(Camera camera, Vec3 position) {
		parent = camera;
		this.position = position;
	}

	public MdlxCamera toMdlx(MdlxCamera camera, EditableModel model) {
		timelinesToMdlx(camera, model);

		return camera;
	}

	public Vec3 getPosition() {
		return position;
	}

	public void setPosition(Vec3 position) {
		this.position.set(position);
	}
	public void setPivotPoint(Vec3 position) {
		this.position.set(position);
	}
	public Vec3 getPivotPoint() {
		return position;
	}

	public Camera getParent() {
		return parent;
	}

	public abstract CameraNode deepCopy(Camera camera);

	@Override
	public Vec3 getRenderScale(TimeEnvironmentImpl animatedRenderEnvironment) {
		return Vec3.ONE;
	}

	@Override
	public float getRenderVisibility(TimeEnvironmentImpl animatedRenderEnvironment) {
		return 1;
	}

	protected void copy(CameraNode node){
		position.set(node.getPosition());
		for (AnimFlag<?> animFlag : node.getAnimFlags()) {
			add(animFlag.deepCopy());
		}
	}

	public static class SourceNode extends CameraNode {

		protected float[] bindPose;

		public SourceNode(Camera camera) {
			super(camera);
		}
		public SourceNode(Camera camera, Vec3 position) {
			super(camera, position);
		}

		private SourceNode(Camera camera, SourceNode node) {
			super(camera);
			copy(node);
		}

		public String getName1() {
			return "Source of: " + parent.getName();
		}

		@Override
		public SourceNode deepCopy(Camera camera) {
			return new SourceNode(camera, this);
		}

		@Override
		public Quat getRenderRotation(TimeEnvironmentImpl animatedRenderEnvironment) {
			AnimFlag<?> rotationFlag = find(MdlUtils.TOKEN_ROTATION);
			if (rotationFlag instanceof IntAnimFlag) {
				int angle = (Integer) rotationFlag.interpolateAt(animatedRenderEnvironment);
				rotationHeap.setFromAxisAngle(getRotationAxis(animatedRenderEnvironment), angle);
				return rotationHeap;
			} else if (rotationFlag instanceof FloatAnimFlag) {
				Float angle = (Float) rotationFlag.interpolateAt(animatedRenderEnvironment);
				rotationHeap.setFromAxisAngle(getRotationAxis(animatedRenderEnvironment), angle);
				return rotationHeap;
			} else if (rotationFlag instanceof QuatAnimFlag) {
				return (Quat) rotationFlag.interpolateAt(animatedRenderEnvironment);
			}
			return null;
		}

		public Vec3 getRotationAxis(TimeEnvironmentImpl animatedRenderEnvironment) {
			Vec3 targetTranslation = parent.getTargetNode().getRenderTranslation(animatedRenderEnvironment);
			Vec3 targetPosition = parent.getTargetPosition();
			Vec3 sourceTranslation = getRenderTranslation(animatedRenderEnvironment);
			axisHeap.set(targetPosition).add(targetTranslation).sub(position).sub(sourceTranslation);
			return axisHeap;
		}

		public float getRenderRotationScalar(TimeEnvironmentImpl animatedRenderEnvironment) {
			AnimFlag<?> rotationFlag = find(MdlUtils.TOKEN_ROTATION);
			if(rotationFlag instanceof IntAnimFlag){
				return getInterpolatedInteger(animatedRenderEnvironment, MdlUtils.TOKEN_ROTATION, 0);
			} else if(rotationFlag instanceof FloatAnimFlag){
				return getInterpolatedFloat(animatedRenderEnvironment, MdlUtils.TOKEN_ROTATION, 0);
			}
			return 0;
		}

		public float[] getBindPose() {
			return bindPose;
		}

		public void setBindPose(float[] bindPose) {
			this.bindPose = bindPose;
		}
	}

	public static class TargetNode extends CameraNode {

		public TargetNode(Camera camera) {
			super(camera);
		}

		public TargetNode(Camera camera, Vec3 position) {
			super(camera, position);
		}

		private TargetNode(Camera camera, TargetNode node) {
			super(camera);
			copy(node);
		}

		public String getName1() {
			return "Target of: " + parent.getName();
		}

		@Override
		public TargetNode deepCopy(Camera camera) {
			return new TargetNode(camera, this);
		}

		@Override
		public Quat getRenderRotation(TimeEnvironmentImpl animatedRenderEnvironment) {
			return Quat.IDENTITY;
		}
	}
}
