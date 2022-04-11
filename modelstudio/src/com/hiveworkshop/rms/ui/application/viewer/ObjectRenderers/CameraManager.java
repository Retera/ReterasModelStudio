package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.render3d.RenderNodeCamera;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.util.*;

import java.awt.*;
import java.awt.event.MouseWheelEvent;

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
public class CameraManager extends CameraHandler {
//public class CameraManager {
	protected final float[] cameraPositionTemp = new float[3];
	protected final float[] cameraTargetTemp = new float[3];
	protected ViewerCamera camera;
	protected float zoomFactor;
	public float horizontalAngle;
	public float verticalAngle;
	public float distance;
	protected Vec3 camPosition = new Vec3();
	protected Vec3 camRight = new Vec3();
	protected Vec3 camUp = new Vec3();
	protected Vec3 camForward = new Vec3();
	protected Vec3 camBackward = new Vec3();
	protected Vec3 target = new Vec3(0, 0, 0);
	protected Vec3 worldUp = new Vec3(0, 0, 1);
	protected Vec3 vecHeap = new Vec3();
	protected Vec4 vec4Heap = new Vec4();
	protected Quat quatHeap = new Quat();
	protected Quat totRot = new Quat();
	protected Quat upRot = new Quat();
	protected Quat sideRot = new Quat();

	private boolean allowToggleOrtho = true;
	private boolean allowRotation = true;
	private boolean isOrtho = false;

	public CameraManager(Component viewport) {
		super(viewport);
		camera = new ViewerCamera();
		this.zoomFactor = 0.1f;
//		this.horizontalAngle = (float) Math.toRadians(90 - 34);
		this.horizontalAngle = (float) Math.toRadians(90);
		this.verticalAngle = (float) (Math.PI / 2);
//		this.distance = 1650;
		this.distance = 3;
		calculateCameraRotation();
	}

	public CameraManager loadDefaultCameraFor(double boundsRadius){
		this.horizontalAngle = (float) Math.toRadians(90);
		this.verticalAngle = (float) (Math.PI / 2);
		calculateCameraRotation();
//		distance = (float) (boundsRadius * Math.sqrt(2)) * 2;
		distance = 3;
//		target.set(0, 0, (float) boundsRadius / 2);
		target.set(0, 0, 0);
		camBackward.set(camPosition).sub(target).normalize();
		camRight.set(Vec3.Z_AXIS).cross(camBackward).normalize();
		return this;
	}
	public void updateCamera() {

		camPosition.set(Vec3.Z_AXIS).transform(totRot).scale(this.distance).add(target);
		camBackward.set(camPosition).sub(target).normalize();
//		position.set(Vec3.X_AXIS).transform(totRot).scale(this.distance).add(target);
		camUp.set(Vec3.Z_AXIS).transform(totRot);
		camRight.set(Vec3.X_AXIS).transform(totRot);
//		forward.set(Vec3.NEGATIVE_Z_AXIS).transform(totRot);
		camForward.set(Vec3.Z_AXIS).transform(totRot);
//		up.set(Vec3.Z_AXIS).transform(totRot);
//		right.set(Vec3.Y_AXIS).transform(totRot);
//		forward.set(Vec3.NEGATIVE_X_AXIS).transform(totRot);
//		worldUp.set(Vec3.Y_AXIS).transform(totRot);
//		worldUp.set(Vec3.Z_AXIS).transform(totRot);
//		worldUp.set(forward).cross(right).normalize();



		camera.moveToAndFace(camPosition, target, worldUp);
		camera.update();
	}

	public void setUpCamera() {
		updateCamera();
//		gluPerspective((float) Math.toDegrees(renderNode.getFoV()), 1, (float) renderNode.getNearClip(), (float) renderNode.getFarClip());
//
//		cameraPos.set(renderNode.getPivot());
//		Vec3 cameraLookAt = renderNode.getTarget();
//		Vec3 up = renderNode.getCameraUp();
//		GLU.gluLookAt(cameraPos.x, cameraPos.y, cameraPos.z, cameraLookAt.x, cameraLookAt.y, cameraLookAt.z, up.x, up.y, up.z);

	}

	public CameraManager setCamera(RenderNodeCamera cameraNode) {
		return this;
	}

	public ViewerCamera getCamera() {
		return camera;
	}

	public Vec3 getTarget() {
		return target;
	}

	public Mat4 getViewProjectionMatrix(){
		return camera.getViewProjectionMatrix();
	}

	public double sizeAdj() {
		return distance / 600f;
	}

	private final Mat4 screenDimensionMat3Heap = new Mat4();
	private final Vec3 screenDimension = new Vec3();
	public void applyPan(double dx, double dy) {
		screenDimension.set(dx, dy, 0);
		screenDimensionMat3Heap.set(camera.getViewProjectionMatrix());
		screenDimensionMat3Heap.transpose();
		screenDimension.transform(0, screenDimensionMat3Heap);
		screenDimension.normalize();
//		screenDimension.scale(0.008f * distance);
		target.add(screenDimension);
	}
	public void setPosition(double a, double b) {
		target.y = (float) a;
		target.z = (float) b;
	}

	public void translate(double right, double up) {
//		location.y += right * location.x / 600f;
//		location.z += up * location.x / 600f;
//		this.dirty = true;
		applyPan(right, up);
	}

	public void translate2(double dx, double dy, double dz) {
		screenDimension.set(dx, dy, dz);
		screenDimensionMat3Heap.set(camera.getViewProjectionMatrix());
		screenDimensionMat3Heap.transpose();
		screenDimension.transform(0, screenDimensionMat3Heap);
		screenDimension.normalize();
//		screenDimension.scale(0.008f * distance);
		target.add(screenDimension);
	}


	public void rotate(double dx, double dy){
		if(allowRotation){
			horizontalAngle -= Math.toRadians(dy);
			verticalAngle -= Math.toRadians(dx);
			calculateCameraRotation();
		} else {
			applyPan(dx, dy);
		}
	}
	public void setCameraRotation(float right, float up) {
		if (allowRotation) {
			horizontalAngle = (float) Math.toRadians(right);
			verticalAngle = (float) Math.toRadians(up);
			calculateCameraRotation();
		}
	}

	public void rot(float rx, float ry, float rz) {
//		xAngle += rx;
		verticalAngle += ry;
		horizontalAngle += rz;
		calculateCameraRotation();
//		System.out.println(""
//				+ "xAngle: " + xAngle
//				+ ", yAngle: " + yAngle
//				+ ", zAngle: " + zAngle);
//		calculateCameraRotation();

//		cameraPos.rotate(cameraPos, Math.toRadians(ry), (byte) 0, (byte) 2);
//		cameraPos.rotate(cameraPos, Math.toRadians(rz), (byte) 1, (byte) 2);
	}

	public void doZoom(MouseWheelEvent e) {
		int wr = e.getWheelRotation();
		int dir = wr < 0 ? -1 : 1;

		for (int i = 0; i < wr * dir; i++) {
			if (dir == -1) {
				distance *= 1.15;
//				m_zoom *= ZOOM_FACTOR;
//				cameraPos.y *= ZOOM_FACTOR;
//				cameraPos.z *= ZOOM_FACTOR;
			} else {
				distance /= 1.15;
//				m_zoom /= ZOOM_FACTOR;
//				cameraPos.y /= ZOOM_FACTOR;
//				cameraPos.z /= ZOOM_FACTOR;
			}
		}
	}

//	public CameraManager zoom(double v){
//		distance *= v;
//		return this;
//	}
	public void zoom(double v){
		distance *= v;
	}

	public double getZoom(){
		return distance;
	}

	public float getXAngle() {
		return 0;
	}

	public float getYAngle() {
		return verticalAngle;
	}

	public float getZAngle() {
		return horizontalAngle;
	}

	public CameraManager resetZoom(){
		distance = 1650;
		return this;
	}


	public boolean isOrtho() {
		return isOrtho;
	}

	public CameraManager setOrtho(boolean ortho){
		this.isOrtho = ortho;
		if(ortho){
			camera.setOrtho();
		} else {
			camera.setPerspective();
		}
		return this;
	}
	public CameraManager toggleOrtho(){
		if(allowToggleOrtho){
			setOrtho(!isOrtho);
		}
		return this;
	}
	public CameraManager setAllowToggleOrtho(boolean allowToggleOrtho) {
		this.allowToggleOrtho = allowToggleOrtho;
		return this;
	}

	public void viewport(float x, float y, float width, float height) {
		camera.viewport(x, y, width, height);
	}


	public Vec3 getGeoPoint(double viewX, double viewY) {
//		double x_real = (viewX - (camera.rect.getWidth() / 2.0)) * distance / 600f + cameraPos.y;
//		double y_real = -(viewY - (camera.rect.getHeight() / 2.0)) * distance / 600f + cameraPos.z;
		double x_real = (viewX - (camera.rect.getWidth() / 2.0));
		double y_real = -(viewY - (camera.rect.getHeight() / 2.0));

		Vec3 vec3 = new Vec3(0, x_real, y_real);

//		vec3.transform(getViewPortAntiRotMat2());
		vec3.transformInverted(getViewProjectionMatrix());
//		System.out.println("GeoMouse: [" + viewX + ", " + viewY + "], " + "zoom: " + m_zoom + ", geoP: " + vec3 + ", camPos: " + cameraPos);

		return vec3;
	}

	public Vec2 getPoint_ifYZplane(double viewX, double viewY) {
		double x_real = (viewX - (camera.rect.getWidth() / 2.0)) + target.y;
		double y_real = -(viewY - (camera.rect.getHeight() / 2.0)) + target.z;

		Vec2 vec2 = new Vec2(x_real, y_real);
		vec2.scale((float) (1f / distance));
//		vec2.transform(getViewPortAntiRotMat2());

//		System.out.println("CamSpaceMouse: [" + viewX + ", " + viewY + "], " + "zoom: " + m_zoom + ", CamP: " + vec2 + ", camPos: " + cameraPos);

		return vec2;
	}

	public void setViewportCamera(int dist, int side, int height, int rX, int rY, int rZ) {
//		xAngle = rX;
		verticalAngle = rY;
		horizontalAngle = rZ;
		calculateCameraRotation();

		target.set(dist, side, height);
//		calculateCameraRotation();

	}


	private void calculateCameraRotation() {
		upRot.setFromAxisAngle(Vec3.X_AXIS, horizontalAngle);
		sideRot.setFromAxisAngle(Vec3.Z_AXIS, verticalAngle);
//		totRot.set(sideRot).mul(upRot);
//		totRot.set(sideRot).mulLeft(upRot);
		totRot.set(upRot).mulLeft(sideRot);
		inverseCameraRotXSpinY.setFromAxisAngle(Vec3.X_AXIS, verticalAngle).normalize();
		inverseCameraRotXSpinY.invertRotation();
		inverseCameraRotYSpinY.setFromAxisAngle(Vec3.Y_AXIS, verticalAngle).normalize();
		inverseCameraRotYSpinY.invertRotation();
//		inverseCameraRotZSpinX.setFromAxisAngle(Vec3.Z_AXIS, (float) Math.toRadians(xAngle)).normalize();
		inverseCameraRotZSpinX.setFromAxisAngle(Vec3.Z_AXIS, 0.0f).normalize();
		inverseCameraRotZSpinX.invertRotation();
		inverseCameraRotZSpinZ.setFromAxisAngle(Vec3.Z_AXIS, horizontalAngle).normalize();
		inverseCameraRotZSpinZ.invertRotation();
		inverseCameraRotation.set(getInverseCameraRotZSpinZ()).mul(getInverseCameraRotYSpinY()).normalize();

	}
	private final Mat4 viewPortAntiRotMat = new Mat4();
	private final Mat4 viewPortMat = new Mat4();
	private final Quat inverseCameraRotation = new Quat();
	private final Quat inverseCameraRotXSpinY = new Quat();
	private final Quat inverseCameraRotYSpinY = new Quat();
	private final Quat inverseCameraRotZSpinX = new Quat();
	private final Quat inverseCameraRotZSpinZ = new Quat();
	public Quat getInverseCameraRotation() {
		return inverseCameraRotation;
	}

	public Quat getInverseCameraRotXSpinY() {
		return inverseCameraRotXSpinY;
	}

	public Quat getInverseCameraRotYSpinY() {
		return inverseCameraRotYSpinY;
	}

	public Quat getInverseCameraRotZSpinX() {
		return inverseCameraRotZSpinX;
	}

	public Quat getInverseCameraRotZSpinZ() {
		return inverseCameraRotZSpinZ;
	}

	public Mat4 getViewPortAntiRotMat() {
		viewPortAntiRotMat.setIdentity().fromQuat(inverseCameraRotation.invertRotation());
		inverseCameraRotation.invertRotation();
		return viewPortAntiRotMat;
	}

	public Mat4 getViewPortAntiRotMat2() {
		viewPortAntiRotMat.setIdentity().fromQuat(inverseCameraRotation);
		return viewPortAntiRotMat;
	}

	Quat quatHeap1 = new Quat();
	Quat quatHeap2 = new Quat();
	Vec3 scaleHeap = new Vec3();
	public Mat4 getViewportMat(){
		// Rotating camera to have +Z up and +X as forward (pointing into camera)
		quatHeap1.setFromAxisAngle(Vec3.X_AXIS, (float) Math.toRadians(-90));
		quatHeap2.setFromAxisAngle(Vec3.Z_AXIS, (float) Math.toRadians(-90));

		quatHeap.set(quatHeap1).mul(quatHeap2);
		quatHeap.mul(quatHeap1);

		float xAngle = 0;
		float yAngle = verticalAngle;
		float zAngle = horizontalAngle;

		quatHeap1.setFromAxisAngle(Vec3.X_AXIS, (float) Math.toRadians(xAngle));
		quatHeap.mul(quatHeap1);
		quatHeap1.setFromAxisAngle(Vec3.Y_AXIS, (float) Math.toRadians(yAngle));
		quatHeap.mul(quatHeap1);
		quatHeap1.setFromAxisAngle(Vec3.Z_AXIS, (float) Math.toRadians(zAngle));
		quatHeap.mul(quatHeap1);

		scaleHeap.set(distance, distance, distance);

		viewPortMat.fromRotationTranslationScaleOrigin(quatHeap, Vec3.Z_AXIS, scaleHeap, target);

		return viewPortMat;
	}

	public Vec3 getCameraLookAt() {
		return target;
	}
}