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
	protected Vec3 worldUp = new Vec3(0, 0, 1);
//	protected Vec3 worldUp = new Vec3(0, 1, 0);
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
	private final Mat4 inverseProjectionMatrix = new Mat4();     // View -> Clip
	private final Mat4 inverseViewProjectionMatrix = new Mat4(); // Clip -> World


	private final Mat4 tempMat4 = new Mat4();
	private final Mat4 viewPortAntiRotMat = new Mat4();
	private final Vec3 screenDimension = new Vec3();

	private final Quat inverseCameraRotation = new Quat();
	private final Quat inverseCameraRotXSpinY = new Quat();
	private final Quat inverseCameraRotYSpinY = new Quat();
	private final Quat inverseCameraRotZSpinX = new Quat();
	private final Quat inverseCameraRotZSpinZ = new Quat();

	private final Quat quatHeap1 = new Quat();
	private final Quat quatHeap2 = new Quat();
	private final Vec3 scaleHeap = new Vec3();


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


		vecHeap.set(camPosition).negate();
		tempMat4.setIdentity().translate(vecHeap);

		viewMatrix.set(camRight, camUp, camBackward);
		viewMatrix.mul(tempMat4);

		float aspect = (float) viewport.getWidth()/(float) viewport.getHeight();
		if (isOrtho){
			projectionMatrix.setOrtho(-aspect*distance/2.0f, aspect*distance/2.0f, -distance/2.0f, distance/2.0f, -6000, 6000);
		} else {
//			projectionMatrix.setPerspective((float) Math.toRadians(70), aspect, 0.0001f, 200000f);
			projectionMatrix.setPerspective((float) Math.toRadians(70), aspect, 1f, 20000f);
		}
		viewProjectionMatrix.set(projectionMatrix).mul(viewMatrix);
	}

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

	public double sizeAdj() {
		Mat4 invProjectionMat = getInvProjectionMat();
		vecHeap.set(1.0 / ((float) viewport.getWidth()), 0, 0);
		vecHeap.transform(invProjectionMat, 1, true);
		float x = vecHeap.x;
		vecHeap.set(0,0,0).transform(invProjectionMat, 1, true);
		return x-vecHeap.x;
	}
	public void setPosition(double a, double b) {
		target.y = (float) a;
		target.z = (float) b;
	}

	public void resetCamera(){
		target.set(Vec3.ZERO);
		upAngle = 0;
		sideAngle = 0;
		tiltAngle = 0;
		distance = 1000;
		calculateCameraRotation();
	}

	public void translate(double right, double up) {
		applyPan(right, up, 0);
//		applyPan(0, right, up);
	}
	public void translate2(double dx, double dy, double dz) {
		applyPan(dx, dy, dz);
	}

	private void applyPan(double dx, double dy, double dz) {
//		screenDimension.set(-dx, -dy, dz);
////		screenDimension.set(-dx*viewport.getWidth(), -dy*viewport.getHeight(), dz).scale(1/distance);
////		vec4Heap.set(0,0,0,1).transform(viewProjectionMatrix);
////		screenDimension.set(-dx*viewport.getWidth()/2.0/vec4Heap.w, -dy*viewport.getHeight()/2.0/vec4Heap.w, dz);
////		screenDimension.set(-dx, -dy, dz);
////		screenDimensionMat3Heap.set(viewProjectionMatrix);
////		screenDimensionMat3Heap.transpose();
////		screenDimension.transform(0, screenDimensionMat3Heap);
//		screenDimension.transform(inverseViewProjectionMatrix, 1, true);
//		screenDimension.set(getWorldScreenSpace1(dx,dy));
		screenDimension.set(getWorldScreenSpace(dx,-dy)).sub(getWorldScreenSpace(0,0));
		System.out.println(screenDimension);
//		screenDimension.normalize();
		target.add(screenDimension);
//		target.set(screenDimension);
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
			tiltAngle = (float) Math.toRadians(0);
			calculateCameraRotation();
			System.out.println("rot: " + Math.toDegrees(upAngle) + ", " + Math.toDegrees(sideAngle) + ", " + Math.toDegrees(tiltAngle)
					+ ", dist: " + distance
					+ ", target: " + target
			);
		}
	}

	public void rot(double rx, double ry, double rz) {
//		tiltAngle += Math.toRadians(rx);
//		sideAngle -= Math.toRadians(ry);
//		upAngle -= Math.toRadians(rz);
		tiltAngle += rx;
		sideAngle -= ry;
		upAngle -= rz;
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
//		distance = 1650;
		distance = 1000;
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
		return getWorldScreenSpace(viewX,viewY);
	}

	private boolean PointInOrOn(Vec3 P1, Vec3 P2, Vec3 A, Vec3 B) {
		Vec3 P1_sub_A = new Vec3(P1).sub(A);
		Vec3 P2_sub_A = new Vec3(P2).sub(A);
		Vec3 CP1 = new Vec3(B).sub(A).cross(P1_sub_A);
		Vec3 CP2 = new Vec3(B).sub(A).cross(P2_sub_A);
		return CP1.dot(CP2) >= 0;
	}

	private boolean PointInOrOnTriangle(Vec3 P, Vec3 A, Vec3 B, Vec3 C ) {
		return PointInOrOn( P, A, B, C ) &&
				PointInOrOn( P, B, C, A ) &&
				PointInOrOn( P, C, A, B );
	}

	private Vec3 getWorldScreenSpace(double viewX, double viewY){
		// https://stackoverflow.com/questions/45893277/is-it-possible-get-which-surface-of-cube-will-be-click-in-opengl
		// https://www.3dgep.com/understanding-the-view-matrix/
		// https://gamedev.stackexchange.com/questions/23395/how-to-convert-screen-space-into-3d-world-space
		// https://stackoverflow.com/questions/7692988/opengl-math-projecting-screen-space-to-world-space-coords
		// https://www.tomdalling.com/blog/modern-opengl/explaining-homogenous-coordinates-and-projective-geometry/

		Mat4 invViewProjectionMat = getInvViewProjectionMat();
		Vec3 nearWorldSpace = new Vec3(viewX, viewY, -1).transform(invViewProjectionMat, 1, true);
		Vec3 farWorldSpace = new Vec3(viewX, viewY, 1).transform(invViewProjectionMat, 1, true);


		// Create a ray from the near clip plane to the far clip plane.
		Vec3 dir = new Vec3(farWorldSpace).sub(nearWorldSpace).normalize();

		// Create a ray.
		Vec3 rayPoint = nearWorldSpace;

		// Calculate the ray-plane intersection point.
		Vec3 planeNorm = new Vec3(camBackward).normalize();
		System.out.println("planeNorm: " + planeNorm);

		float pD = -target.dot(planeNorm);

		// Calculate distance of intersection point from r.origin.
		float denominator = planeNorm.dot(dir);
		float numerator = planeNorm.dot(rayPoint) + pD;
		float t = -(numerator / denominator);
		System.out.println("t: " + t);

		// Calculate the picked position on the y = 0 plane.
		return nearWorldSpace.addScaled(dir,t);
	}


	private Vec3 getRayFromScreenSpace(double viewX, double viewY){
		Mat4 invViewProjectionMat = getInvViewProjectionMat();
		Vec3 nearWorldSpace = new Vec3(viewX, viewY, -1).transform(invViewProjectionMat, 1, true);
		Vec3 farWorldSpace = new Vec3(viewX, viewY, 1).transform(invViewProjectionMat, 1, true);

		// Create a ray from the near clip plane to the far clip plane.
		Vec3 dir = new Vec3(farWorldSpace).sub(nearWorldSpace).normalize();

		// Create a ray.
		Vec3 rayPoint = nearWorldSpace;
		return dir;
	}

	public Vec2 getPoint_ifYZplane(double viewX, double viewY) {
//		double x_real = (viewX - (camera.rect.getWidth() / 2.0)) + target.y;
//		double y_real = -(viewY - (camera.rect.getHeight() / 2.0)) + target.z;
		double x_real = (viewX - (viewport.getWidth() / 2.0)) + target.y;
		double y_real = -(viewY - (viewport.getHeight() / 2.0)) + target.z;
		float xRatio = 2.0f * (float) viewX / (float) viewport.getWidth() - 1.0f;
		float yRatio = 1.0f - 2.0f * (float) viewX / (float) viewport.getHeight();

		Vec2 vec2 = new Vec2(x_real, y_real);
//		vec2.scale((float) (1f / distance));
//		vec2.transform(getViewPortAntiRotMat2());

//		System.out.println("CamSpaceMouse: [" + viewX + ", " + viewY + "], " + "zoom: " + m_zoom + ", CamP: " + vec2 + ", camPos: " + cameraPos);
//		Vec2 vec22 = new Vec2(viewX - (viewport.getWidth() / 2.0), -(viewY - (viewport.getHeight() / 2.0)));
//		Vec2 vec22 = new Vec2(viewX*(float)viewport.getWidth(), viewY*(float)viewport.getHeight());
		Vec2 vec22 = new Vec2(xRatio, yRatio);

		return vec22;
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
//		viewPortAntiRotMat.setIdentity().fromQuat(inverseCameraRotation.invertRotation());
//		inverseCameraRotation.invertRotation();
//		return viewPortAntiRotMat;

		viewPortAntiRotMat.set(viewProjectionMatrix);
//		viewPortAntiRotMat.set(testSelMatrix);
		return viewPortAntiRotMat;
	}

	public Mat4 getViewPortAntiRotMat2() {
//		viewPortAntiRotMat.setIdentity().fromQuat(inverseCameraRotation);
//		viewPortAntiRotMat.set(viewProjectionMatrix).invert();

		viewPortAntiRotMat.set(viewProjectionMatrix);
//		viewPortAntiRotMat.set(testSelMatrix);
		return viewPortAntiRotMat;
	}

	public Mat4 getViewProjectionMatrix(){
		return viewProjectionMatrix;
	}

	public Mat4 getViewProjectionMat(){
		return viewProjectionMatrix;
	}

	public Mat4 getInvViewProjectionMat(){
		inverseViewProjectionMatrix.set(viewProjectionMatrix).invert();
		return inverseViewProjectionMatrix;
	}

	public Mat4 getProjectionMat(){
		return projectionMatrix;
	}
	public Mat4 getInvProjectionMat(){
		inverseProjectionMatrix.set(projectionMatrix).invert();
		return inverseProjectionMatrix;
	}

	public Mat4 getViewMat(){
		return viewMatrix;
	}
	public Mat4 getInvViewMat(){
		inverseViewMatrix.set(viewMatrix).invert();
		return inverseViewMatrix;
	}

	public Vec3 getCameraLookAt() {
		return target;
	}
}