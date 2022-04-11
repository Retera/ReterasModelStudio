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
	protected float upAngle;    // pitch
	protected float sideAngle;  // yaw
	protected float tiltAngle;  // roll
	protected float distance;
	protected Vec3 camPosition = new Vec3();
	protected Vec3 camRight = new Vec3();
	protected Vec3 camUp = new Vec3();
	protected Vec3 camForward = new Vec3();
	protected Vec3 camBackward = new Vec3();
	protected Vec3 target = new Vec3(0, 0, 0);
//	protected Vec3 worldUp = new Vec3(0, 0, 1);
	protected Vec3 worldUp = new Vec3(0, 1, 0);
	protected Vec3 vecHeap = new Vec3();
	protected Vec4 vec4Heap = new Vec4();
	protected Quat quatHeap = new Quat();
	protected Quat totRot = new Quat();
	protected Quat upRot = new Quat();      // pitch
	protected Quat sideRot = new Quat();    // yaw
	protected Quat tilt = new Quat();       // roll

	private boolean allowToggleOrtho = true;
	private boolean allowRotation = true;
	private boolean isOrtho = false;

	private final Mat4 viewMatrix = new Mat4();                  // World -> View
	private final Mat4 projectionMatrix = new Mat4();            // View -> Clip
	private final Mat4 viewProjectionMatrix = new Mat4();        // World -> Clip
	private final Mat4 inverseViewMatrix = new Mat4();           // View -> World
	private final Mat4 inverseViewProjectionMatrix = new Mat4(); // Clip -> World
	private final Component viewport;
	// https://learnopengl.com/Getting-started/Camera
	public CameraManager(Component viewport) {
		super(viewport);
		this.viewport = viewport;
		camera = new ViewerCamera();
		this.zoomFactor = 0.1f;
//		this.horizontalAngle = (float) Math.toRadians(90 - 34);
		this.upAngle = (float) Math.toRadians(90);
		this.sideAngle = (float) (Math.PI / 2);
		this.distance = 1650;
//		this.distance = 3;
		calculateCameraRotation();
	}

	public CameraManager loadDefaultCameraFor(double boundsRadius){
//		this.horizontalAngle = (float) Math.toRadians(90);
//		this.verticalAngle = (float) (Math.PI / 2);
		this.upAngle = 0.0f;
		this.sideAngle = 0.0f;
//		calculateCameraRotation();
		distance = (float) (boundsRadius * Math.sqrt(2));
//		distance = 3;
		target.set(0, 0, (float) boundsRadius / 2);
//		target.set(0, 0, 0);
		camPosition.set(distance, 0,0).add(target);
		camBackward.set(camPosition).sub(target).normalize();
		camRight.set(worldUp).cross(camBackward).normalize();
		calculateCameraRotation();
		return this;
	}
	public void updateCamera() {

		worldUp.set(Vec3.Z_AXIS);
		camPosition.set(1,0,0).transform(totRot).scale(distance).add(target);
		camUp.set(0,0,1).transform(totRot);

		camBackward.set(camPosition).sub(target).normalize();
		camRight.set(camUp).cross(camBackward).normalize();
		camUp.set(camBackward).cross(camRight).normalize();


		viewMatrix.setIdentity();
		viewMatrix.set(camRight, camUp, camBackward);

		tempMat4.setIdentity();
		camPosition.negate();
		tempMat4.translate(camPosition);
		camPosition.negate();

		viewMatrix.mul(tempMat4);


		float aspect = (float) viewport.getWidth()/(float) viewport.getHeight();
//		projectionMatrix.setOrtho(-viewport.getWidth()/2, viewport.getWidth()/2, 0, viewport.getHeight(), -50, 60000);
//		projectionMatrix.setOrtho(-viewport.getWidth()/2, viewport.getWidth()/2, 0, -viewport.getHeight(), -50, 60000);
		if (isOrtho){
			projectionMatrix.setOrtho(-aspect*distance/2.0f, aspect*distance/2.0f, -distance/2.0f, distance/2.0f, -6000, 6000);
		} else {
			projectionMatrix.setPerspective((float) Math.toRadians(70), aspect, 0.0001f, 200000f);
		}
		viewProjectionMatrix.set(projectionMatrix).mul(viewMatrix);

//		camera.moveToAndFace(camPosition, target, worldUp);
//		camera.update();
	}

	private final Mat4 tempMat4 = new Mat4();
	public void setToLookAt(Vec3 camPosition, Vec3 target, Vec3 worldUp) {
		camBackward.set(camPosition).sub(target).normalize();
		camRight.set(camBackward).cross(worldUp).normalize();
		if (camRight.lengthSquared() <= 0) {
			viewMatrix.setIdentity();
			System.err.println("bad setToLookAt: " + camPosition + ", " + target + ", " + worldUp);
			return;
		}

		camUp.set(camRight).cross(camBackward).normalize();

		viewMatrix.setIdentity();
		viewMatrix.m00 = camRight.x;
		viewMatrix.m01 = camRight.y;
		viewMatrix.m02 = camRight.z;
		viewMatrix.m10 = camUp.x;
		viewMatrix.m11 = camUp.y;
		viewMatrix.m12 = camUp.z;
		viewMatrix.m20 = -camBackward.x;
		viewMatrix.m21 = -camBackward.y;
		viewMatrix.m22 = -camBackward.z;

		tempMat4.setIdentity();
		tempMat4.translate(camPosition);

		viewMatrix.mul(tempMat4);
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

//	public ViewerCamera getCamera() {
//		return camera;
//	}

	public Vec3 getTarget() {
		return target;
	}

	public Mat4 getViewProjectionMatrix(){
//		return viewMatrix;
		return viewProjectionMatrix;
//		return camera.getViewProjectionMatrix();
	}

	public double sizeAdj() {
		return distance / 600f;
	}
	public void setPosition(double a, double b) {
		target.y = (float) a;
		target.z = (float) b;
	}

	public void translate(double right, double up) {
		applyPan(right, up, 0);
//		applyPan(0, right, up);
	}
	public void translate2(double dx, double dy, double dz) {
		applyPan(dx, dy, dz);
	}
	private final Mat4 screenDimensionMat3Heap = new Mat4();
	private final Vec3 screenDimension = new Vec3();
	private void applyPan(double dx, double dy, double dz) {
//		screenDimension.set(-dx, dy, dz).scale(1/distance);
		screenDimension.set(-dx, dy, dz);
//		screenDimensionMat3Heap.set(viewProjectionMatrix);
//		screenDimensionMat3Heap.transpose();
//		screenDimension.transform(0, screenDimensionMat3Heap);
		screenDimension.transformInverted(viewProjectionMatrix);
		screenDimension.normalize();
		target.add(screenDimension);
	}


	public void rotate(double dx, double dy){
		if(allowRotation){
			rot(0, dx, dy);
		} else {
			applyPan(dx, dy, 0);
		}
	}
	public void setCameraRotation(float right, float up) {
		if (allowRotation) {
			upAngle = (float) Math.toRadians(right);
			sideAngle = (float) Math.toRadians(up);
			calculateCameraRotation();
		}
	}

	public void rot(double rx, double ry, double rz) {
		tiltAngle += Math.toRadians(rx);
		sideAngle -= Math.toRadians(ry);
		upAngle -= Math.toRadians(rz);
		calculateCameraRotation();
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
		System.out.println("distance: " + distance);
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
		return sideAngle;
	}

	public float getZAngle() {
		return upAngle;
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
//		if(ortho){
//			camera.setOrtho();
//		} else {
//			camera.setPerspective();
//		}
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
//		camera.viewport(x, y, width, height);
	}


	public Vec3 getGeoPoint(double viewX, double viewY) {
//		double x_real = (viewX - (camera.rect.getWidth() / 2.0)) * distance / 600f + cameraPos.y;
//		double y_real = -(viewY - (camera.rect.getHeight() / 2.0)) * distance / 600f + cameraPos.z;
//		double x_real = (viewX - (camera.rect.getWidth() / 2.0));
//		double y_real = -(viewY - (camera.rect.getHeight() / 2.0));
		double x_real = (viewX - (viewport.getWidth() / 2.0));
		double y_real = -(viewY - (viewport.getHeight() / 2.0));

		Vec3 vec3 = new Vec3(0, x_real, y_real);

//		vec3.transform(getViewPortAntiRotMat2());
		vec3.transformInverted(getViewProjectionMatrix());
//		System.out.println("GeoMouse: [" + viewX + ", " + viewY + "], " + "zoom: " + m_zoom + ", geoP: " + vec3 + ", camPos: " + cameraPos);

		return vec3;
	}

	public Vec2 getPoint_ifYZplane(double viewX, double viewY) {
//		double x_real = (viewX - (camera.rect.getWidth() / 2.0)) + target.y;
//		double y_real = -(viewY - (camera.rect.getHeight() / 2.0)) + target.z;
		double x_real = (viewX - (viewport.getWidth() / 2.0)) + target.y;
		double y_real = -(viewY - (viewport.getHeight() / 2.0)) + target.z;

		Vec2 vec2 = new Vec2(x_real, y_real);
		vec2.scale((float) (1f / distance));
//		vec2.transform(getViewPortAntiRotMat2());

//		System.out.println("CamSpaceMouse: [" + viewX + ", " + viewY + "], " + "zoom: " + m_zoom + ", CamP: " + vec2 + ", camPos: " + cameraPos);

		return vec2;
	}

	public void setViewportCamera(int dist, int side, int height, int rX, int rY, int rZ) {
		tiltAngle = rX;
		sideAngle = rY;
		upAngle = rZ;
		calculateCameraRotation();

		target.set(dist, side, height);
//		calculateCameraRotation();

	}


	private void calculateCameraRotation() {
		upRot.setFromAxisAngle(Vec3.Y_AXIS, upAngle);
//		upRot.setFromAxisAngle(Vec3.X_AXIS, verticalAngle);
		sideRot.setFromAxisAngle(Vec3.Z_AXIS, sideAngle);
//		totRot.set(sideRot).mul(upRot);
//		totRot.set(sideRot).mulLeft(upRot);
		tilt.set(Vec3.X_AXIS, tiltAngle);
		totRot.set(upRot).mulLeft(sideRot);
		inverseCameraRotXSpinY.setFromAxisAngle(Vec3.X_AXIS, sideAngle).normalize();
		inverseCameraRotXSpinY.invertRotation();
		inverseCameraRotYSpinY.setFromAxisAngle(Vec3.Y_AXIS, sideAngle).normalize();
		inverseCameraRotYSpinY.invertRotation();
//		inverseCameraRotZSpinX.setFromAxisAngle(Vec3.Z_AXIS, (float) Math.toRadians(xAngle)).normalize();
		inverseCameraRotZSpinX.setFromAxisAngle(Vec3.Z_AXIS, 0.0f).normalize();
		inverseCameraRotZSpinX.invertRotation();
		inverseCameraRotZSpinZ.setFromAxisAngle(Vec3.Z_AXIS, upAngle).normalize();
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
		float yAngle = sideAngle;
		float zAngle = upAngle;

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