package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.AnimationMap;
import com.hiveworkshop.rms.parsers.mdlx.MdlxCamera;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.rms.util.Vec3;

/**
 * Camera class, these are the things most people would think of as a particle
 * emitter, I think. Blizzard favored use of these over ParticleEmitters and I
 * do too simply because I so often recycle data and there are more of these to
 * use.
 *
 * Eric Theller 3/10/2012 3:32 PM
 */
public class Camera implements Named {
	private String name;
	private double fieldOfView;
	private double farClip;
	private double nearClip;
	private final CameraNode.SourceNode sourceNode;
	private final CameraNode.TargetNode targetNode;
	private float[] bindPose;

	public Camera(MdlxCamera camera, EditableModel model) {
		name = camera.name;
		sourceNode = new CameraNode.SourceNode(this, new Vec3(camera.position));
		targetNode = new CameraNode.TargetNode(this, new Vec3(camera.targetPosition));
		fieldOfView = camera.fieldOfView;
		farClip = camera.farClippingPlane;
		nearClip = camera.nearClippingPlane;
//		System.out.println(timeline.getClass().getSimpleName() + ", name: " + timeline.name + ", vSize: " + timeline.vSize());

		for (MdlxTimeline<?> timeline : camera.timelines) {
			if (timeline.name.equals(AnimationMap.KTTR.getWar3id())) {
				targetNode.add(AnimFlag.createFromTimeline(timeline, model));
			} else {
				sourceNode.add(AnimFlag.createFromTimeline(timeline, model));
			}
		}
	}

	public Camera(String name) {
		this.name = name;

		sourceNode = new CameraNode.SourceNode(this);
		targetNode = new CameraNode.TargetNode(this);
		fieldOfView = Math.toRadians(60);
		farClip = 2000;
		nearClip = 10;
	}

	private Camera(Camera camera) {
		name = camera.name;

		sourceNode = camera.sourceNode.deepCopy(this);
		targetNode = camera.targetNode.deepCopy(this);

		fieldOfView = camera.fieldOfView;
		farClip = camera.farClip;
		nearClip = camera.nearClip;

		for (AnimFlag<?> animFlag : camera.targetNode.getAnimFlags()) {
			targetNode.add(animFlag.deepCopy());
		}
		for (AnimFlag<?> animFlag : camera.sourceNode.getAnimFlags()) {
			sourceNode.add(animFlag.deepCopy());
		}
		bindPose = camera.bindPose;
	}

	public MdlxCamera toMdlx(EditableModel model) {
		MdlxCamera camera = new MdlxCamera();

		camera.name = getName();
		camera.position = getPosition().toFloatArray();
		camera.fieldOfView = (float) getFieldOfView();
		camera.farClippingPlane = (float) getFarClip();
		camera.nearClippingPlane = (float) getNearClip();
		camera.targetPosition = getTargetPosition().toFloatArray();

		sourceNode.timelinesToMdlx(camera, model);
		targetNode.timelinesToMdlx(camera, model);

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
		return sourceNode.getPosition();
	}

	public void setPosition(Vec3 position) {
		sourceNode.setPosition(position);
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
		return targetNode.getPosition();
	}

	public void setTargetPosition(Vec3 targetPosition) {
		targetNode.setPosition(targetPosition);
	}

	public float[] getBindPose() {
		return bindPose;
	}

	public void setBindPose(float[] bindPose) {
		this.bindPose = bindPose;
	}

	public CameraNode.SourceNode getSourceNode() {
		return sourceNode;
	}

	public CameraNode.TargetNode getTargetNode() {
		return targetNode;
	}

	public Camera deepCopy() {
		return new Camera(this);
	}
}
