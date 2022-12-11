package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.AbstractCamera;
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
public class CameraManager extends AbstractCamera {
	private final double ZOOM_FACTOR = 1.07;
	protected float upAngle;    // pitch
	protected float sideAngle;  // yaw
	protected float tiltAngle;  // roll
	protected float distance;

	protected float fieldOfView = 70;
	protected float farClip = 20000f;
	protected float nearClip = 1f;

	protected Vec3 camPosition = new Vec3();
	protected Vec3 camRight = new Vec3();
	protected Vec3 camUp = new Vec3();
	protected Vec3 camBackward = new Vec3();
	protected Vec3 target = new Vec3(0, 0, 0);
	protected Vec3 worldUp = new Vec3(0, 0, 1);
	protected Vec3 vecHeap = new Vec3();
	protected Vec4 vec4Heap = new Vec4();
	protected Quat totRot = new Quat();
	protected Quat upRot = new Quat();      // pitch
	protected Quat sideRot = new Quat();    // yaw
	protected Quat tilt = new Quat();       // roll

	private boolean allowToggleOrtho = true;
	private boolean allowRotation = true;
	private boolean isOrtho = false;

	private final Mat4 cameraSpaceMatrix = new Mat4();           // World -> Camera
	private final Mat4 viewMatrix = new Mat4();                  // World -> View
	private final Mat4 projectionMatrix = new Mat4();            // View -> Clip
	private final Mat4 viewProjectionMatrix = new Mat4();        // World -> Clip
	private final Mat4 inverseViewMatrix = new Mat4();           // View -> World
	private final Mat4 inverseProjectionMatrix = new Mat4();     // Clip -> View
	private final Mat4 inverseViewProjectionMatrix = new Mat4(); // Clip -> World


	private final Mat4 tempMat4 = new Mat4();

	private final Quat inverseCameraRotation = new Quat();
	private final Quat inverseCameraRotXSpinY = new Quat();
	private final Quat inverseCameraRotYSpinY = new Quat();
	private final Quat inverseCameraRotZSpinX = new Quat();
	private final Quat inverseCameraRotZSpinZ = new Quat();

//	protected Vec3 camForward = new Vec3();
//	private final Vec3 screenDimension = new Vec3();
//	protected Quat quatHeap = new Quat();
//	private final Quat quatHeap1 = new Quat();
//	private final Quat quatHeap2 = new Quat();
//	private final Vec3 scaleHeap = new Vec3();


	private final Ray rayHeap = new Ray();
	private final Plane planeHeap = new Plane();
	private final Vec3 worldScreenSpacePoint = new Vec3();


	private final Component viewport;
	// https://learnopengl.com/Getting-started/Camera
	public CameraManager(Component viewport) {
		this.viewport = viewport;
		this.upAngle = (float) Math.toRadians(90);
		this.sideAngle = (float) (Math.PI / 2);
		this.distance = 1650;
//		this.distance = 3;
		calculateCameraRotation();
	}

	public CameraManager loadDefaultCameraFor(double boundsRadius){
		this.upAngle = 0.0f;
		this.sideAngle = 0.0f;
		distance = (float) (boundsRadius * Math.sqrt(2));

		nearClip = Math.min(1.0f, (float) boundsRadius/10f);
		farClip = Math.max(20000f, (float) boundsRadius*5f);
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
		cameraSpaceMatrix.set(camRight, camUp, camBackward);

		vecHeap.set(camPosition).negate();
		tempMat4.setIdentity().translate(vecHeap);

		viewMatrix.set(cameraSpaceMatrix).mul(tempMat4);

		float aspect = (float) viewport.getWidth()/(float) viewport.getHeight();
		if (isOrtho){
			projectionMatrix.setOrtho(-aspect*distance/2.0f, aspect*distance/2.0f, -distance/2.0f, distance/2.0f, -4000, 4000);
		} else {
//			projectionMatrix.setPerspective((float) Math.toRadians(70), aspect, 0.0001f, 200000f);
			projectionMatrix.setPerspective((float) Math.toRadians(fieldOfView), aspect, nearClip, farClip);
		}
		viewProjectionMatrix.set(projectionMatrix).mul(viewMatrix);
	}

	public CameraManager setCamera(RenderModel renderModel, CameraNode cameraNode) {
		return this;
	}

	public Vec3 getTarget() {
		return target;
	}

	// 1 px on screen to distance in world
	public double sizeAdj() {
		Vec3 worldScreenSpaceAsDeltaRay2 = getWorldScreenSpaceAsDeltaRay(1, 0);
		return worldScreenSpaceAsDeltaRay2.length() * 2.0 / ((double) viewport.getWidth());
	}

	private void printPixelSize(){
//		Mat4 invProjectionMat = getInvProjectionMat();
//		vecHeap.set(1.0, 1.0, -1);
//		vecHeap.transform(invProjectionMat, 1, true);
//		System.out.println("{1.0, 1.0} -> " + vecHeap);
//		float x = vecHeap.x;
//		vecHeap.set(0,0,-1).transform(invProjectionMat, 1, true);
//		x -= vecHeap.x;
//		System.out.println("{0.0, 0.0} -> " + vecHeap);
		Vec3 worldScreenSpaceAsDeltaRay = getWorldScreenSpaceAsDeltaRay(1, 1);
		System.out.println("{1.0, 1.0} -> " + worldScreenSpaceAsDeltaRay);
		System.out.println("pixelSize: " + worldScreenSpaceAsDeltaRay.x / ((double) viewport.getWidth()));
		Vec3 worldScreenSpaceAsDeltaRay2 = getWorldScreenSpaceAsDeltaRay(1, 0);
		System.out.println("{1.0, 0.0} -> " + worldScreenSpaceAsDeltaRay2);
		System.out.println("pixelSize: " + worldScreenSpaceAsDeltaRay2.length() / ((double) viewport.getWidth()));
	}
	public void setPosition(double a, double b) {
		printPixelSize();

		target.y = (float) a;
		target.z = (float) b;
	}

	public void setTargetPoint(Vec3 v) {
		target.set(v);
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
		applyPan(right, up);
	}
	public void moveTargetDepth(double dept) {
		vecHeap.set(camBackward).scale((float) dept);
		target.add(vecHeap);
	}

	private void applyPan(double dx, double dy) {
		target.add(getWorldScreenSpaceAsDeltaRay(-dx, -dy));
	}

	public CameraManager setAllowRotation(boolean allowRotation) {
		this.allowRotation = allowRotation;
		return this;
	}
	public void setCameraRotation(float right, float up) {
		setCameraRotation(right, up, 0);
	}
	public void setCameraRotation(float right, float up, float tilt) {
		if (allowRotation) {
			setRotation(right, up, tilt);
		}
	}

	private void setRotation(float right, float up, float tilt) {
		upAngle = (float) Math.toRadians(up);
		sideAngle = (float) Math.toRadians(right);
		tiltAngle = (float) Math.toRadians(tilt);
		calculateCameraRotation();
	}

	public void setCameraRotationRad(float right, float up) {
		if (allowRotation) {
			upAngle = up;
			sideAngle = right;
			tiltAngle = 0f;
			calculateCameraRotation();
		}
	}

	public void rotate(double right, double up){
		if(allowRotation){
			rot(0, right, -up);
		} else {
			applyPan(right, up);
		}
	}

	public void rot(double rx, double ry, double rz) {
		tiltAngle += Math.toRadians(rx);
		sideAngle -= Math.toRadians(ry);
		upAngle -= Math.toRadians(rz);
		calculateCameraRotation();
	}
	public void rotRad(double rx, double ry, double rz) {
		tiltAngle += rx;
		sideAngle -= ry;
		upAngle -= rz;
		calculateCameraRotation();
	}

	public void doZoom(MouseWheelEvent e) {
		int wr = e.getWheelRotation();
		doZoom(wr);
	}

	private void doZoom(int wr) {
		int dir = wr < 0 ? -1 : 1;

		for (int i = 0; i < wr * dir; i++) {
			if (dir == 1) {
				distance *= ZOOM_FACTOR;
//				m_zoom *= ZOOM_FACTOR;
//				cameraPos.y *= ZOOM_FACTOR;
//				cameraPos.z *= ZOOM_FACTOR;
			} else {
				distance /= ZOOM_FACTOR;
//				m_zoom /= ZOOM_FACTOR;
//				cameraPos.y /= ZOOM_FACTOR;
//				cameraPos.z /= ZOOM_FACTOR;
			}
		}
	}

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


	public Vec3 getGeoPoint(Vec2 view) {
		return getGeoPoint(view.x,view.y);
	}
	public Vec3 getGeoPoint(double viewX, double viewY) {
//		System.out.println("camPos: " + camPosition.toRoundedString() + ", target: " + target.toRoundedString());
		return getWorldScreenSpace(viewX,viewY);
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
//		System.out.println("planeNorm: " + planeNorm);

		float pD = -target.dot(planeNorm);

		// Calculate distance of intersection point from r.origin.
		float denominator = planeNorm.dot(dir);
		float numerator = planeNorm.dot(rayPoint) + pD;
		float t = -(numerator / denominator);
//		System.out.println("t: " + t);

		// Calculate the picked position on the y = 0 plane.
		return nearWorldSpace.addScaled(dir,t);
	}

	private Vec3 getWorldScreenSpaceAsDeltaRay(double viewX, double viewY){
		// https://stackoverflow.com/questions/45893277/is-it-possible-get-which-surface-of-cube-will-be-click-in-opengl
		// https://www.3dgep.com/understanding-the-view-matrix/
		// https://gamedev.stackexchange.com/questions/23395/how-to-convert-screen-space-into-3d-world-space
		// https://stackoverflow.com/questions/7692988/opengl-math-projecting-screen-space-to-world-space-coords
		// https://www.tomdalling.com/blog/modern-opengl/explaining-homogenous-coordinates-and-projective-geometry/

		Mat4 invViewProjectionMat = getInvViewProjectionMat();
		// Create a ray from the near clip plane to the far clip plane.
		vecHeap.set(viewX, viewY, -1).transform(invViewProjectionMat, 1, true);
		rayHeap.setPoint(vecHeap);
		vecHeap.set(viewX, viewY, 1).transform(invViewProjectionMat, 1, true);
		rayHeap.setDirFromEnd(vecHeap);

		// Calculate the ray-plane intersection point.
		planeHeap.set(camBackward, target);
		float intersectP = planeHeap.getIntersect(rayHeap);

		// Calculate the picked position on the y = 0 plane.
		worldScreenSpacePoint.set(rayHeap.getPoint()).addScaled(rayHeap.getDir(), intersectP);


		// For 0,0:
		// Create a ray from the near clip plane to the far clip plane.
		vecHeap.set(0, 0, -1).transform(invViewProjectionMat, 1, true);
		rayHeap.setPoint(vecHeap);
		vecHeap.set(0, 0, 1).transform(invViewProjectionMat, 1, true);
		rayHeap.setDirFromEnd(vecHeap);


		float intersectO = planeHeap.getIntersect(rayHeap);

		vecHeap.set(rayHeap.getPoint()).addScaled(rayHeap.getDir(), intersectO);

		return worldScreenSpacePoint.sub(vecHeap);
	}

	SelectionBoxHelper selectionBoxHelper = new SelectionBoxHelper();
	public SelectionBoxHelper getSelectionBoxHelper(Vec2 topRight, Vec2 bottomLeft){
		planeHeap.set(camBackward, target);
		return selectionBoxHelper.setFrom(topRight, bottomLeft, camRight, camUp, planeHeap, getInvViewProjectionMat());
	}

	public Ray getRayFromScreenSpace(double viewX, double viewY){
		Mat4 invViewProjectionMat = getInvViewProjectionMat();
		// Create a ray from the near clip plane to the far clip plane.
		vecHeap.set(viewX, viewY, -1).transform(invViewProjectionMat, 1, true);
		rayHeap.setPoint(vecHeap);
		vecHeap.set(viewX, viewY, 1).transform(invViewProjectionMat, 1, true);
		rayHeap.setDirFromEnd(vecHeap);
		return rayHeap;
	}

	public void setViewportCamera(int dist, int side, int height, int rX, int rY, int rZ) {
		setRotation(rY, rZ, rX);

		target.set(dist, side, height);
	}


	protected void calculateCameraRotation() {
		upRot.setFromAxisAngle(Vec3.Y_AXIS, upAngle);
		sideRot.setFromAxisAngle(Vec3.Z_AXIS, sideAngle);
		tilt.setFromAxisAngle(Vec3.X_AXIS, tiltAngle);
		totRot.set(tilt).mulLeft(upRot).mulLeft(sideRot);

		inverseCameraRotXSpinY.setFromAxisAngle(Vec3.X_AXIS, sideAngle).normalize();
		inverseCameraRotXSpinY.invertRotation();
		inverseCameraRotYSpinY.setFromAxisAngle(Vec3.Y_AXIS, sideAngle).normalize();
		inverseCameraRotYSpinY.invertRotation();
		inverseCameraRotZSpinX.setFromAxisAngle(Vec3.Z_AXIS, 0.0f).normalize();
		inverseCameraRotZSpinX.invertRotation();
		inverseCameraRotZSpinZ.setFromAxisAngle(Vec3.Z_AXIS, upAngle).normalize();
		inverseCameraRotZSpinZ.invertRotation();
		inverseCameraRotation.set(sideRot).mul(upRot).normalize();

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

	@Override
	public Component getComponent() {
		return viewport;
	}
}