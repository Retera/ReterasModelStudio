package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

/**
 * The camera manager class, ported out from Warsmash. It does not have an
 * equivalent in mdx-m3-viewer and was created for Warsmash. The type hierarchy
 * in Warsmash at the time this was copied was:
 *
 * <pre>
 * CameraManager -> GameCameraManager -> PortraitCameraManager
 * </pre>
 *
 * So, conceivably for some kind of "in game preview" in the model editor some
 * day, you might later want to copy GameCameraManager into here??
 */
public abstract class CameraManager {
	private static final double HORIZONTAL_ANGLE_INCREMENT = Math.PI / 60;
	protected final float[] cameraPositionTemp = new float[3];
	protected final float[] cameraTargetTemp = new float[3];
	public ViewerCamera camera;
	protected float moveSpeed;
	protected float rotationSpeed;
	protected float zoomFactor;
	public float horizontalAngle;
	public float verticalAngle;
	public float distance;
	protected Vec3 position;
	public Vec3 target;
	protected Vec3 worldUp;
	protected Vec3 vecHeap;
	protected Vec4 vec4Heap;
	protected Quat quatHeap;
	protected Quat quatHeap2;

	public CameraManager() {
	}

	public void setupCamera(ViewerCamera camera) {
		this.camera = camera;
		this.moveSpeed = 2;
		this.rotationSpeed = (float) HORIZONTAL_ANGLE_INCREMENT;
		this.zoomFactor = 0.1f;
		this.horizontalAngle = (float) Math.toRadians(90 - 34);
		this.verticalAngle = (float) (Math.PI / 2);
		this.distance = 1650;
		this.position = new Vec3();
		this.target = new Vec3(0, 0, 0);
		this.worldUp = new Vec3(0, 0, 1);
		this.vecHeap = new Vec3();
		this.vec4Heap = new Vec4();
		this.quatHeap = new Quat();
		this.quatHeap2 = new Quat();

		updateCamera();
	}

	public abstract void updateCamera();
}