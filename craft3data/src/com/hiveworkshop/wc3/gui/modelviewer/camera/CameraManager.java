package com.hiveworkshop.wc3.gui.modelviewer.camera;

import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.hiveworkshop.wc3.gui.modelviewer.ViewerCamera;

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
	protected Vector3f position;
	public Vector3f target;
	protected Vector3f worldUp;
	protected Vector3f vecHeap;
	protected Vector4f vec4Heap;
	protected Quaternion quatHeap;
	protected Quaternion quatHeap2;

	public CameraManager() {
	}

	public void setupCamera(final ViewerCamera camera/* final Scene scene */) {
		this.camera = camera; // scene.camera;
		this.moveSpeed = 2;
		this.rotationSpeed = (float) HORIZONTAL_ANGLE_INCREMENT;
		this.zoomFactor = 0.1f;
		this.horizontalAngle = (float) Math.toRadians(90 - 34);
		this.verticalAngle = (float) (Math.PI / 2);
		this.distance = 1650;
		this.position = new Vector3f();
		this.target = new Vector3f(0, 0, 0);
		this.worldUp = new Vector3f(0, 0, 1);
		this.vecHeap = new Vector3f();
		this.vec4Heap = new Vector4f();
		this.quatHeap = new Quaternion();
		this.quatHeap2 = new Quaternion();

		updateCamera();

//		cameraUpdate();
	}

	public abstract void updateCamera();

//	private void cameraUpdate() {
//
//	}
}