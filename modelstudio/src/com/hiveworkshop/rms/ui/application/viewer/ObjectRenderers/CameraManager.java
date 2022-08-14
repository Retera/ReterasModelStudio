package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNodeCamera;
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
public class CameraManager {
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
//	protected Vec3 camForward = new Vec3();
	protected Vec3 camBackward = new Vec3();
	protected Vec3 target = new Vec3(0, 0, 0);
	protected Vec3 worldUp = new Vec3(0, 0, 1);
	protected Vec3 vecHeap = new Vec3();
	protected Vec4 vec4Heap = new Vec4();
//	protected Quat quatHeap = new Quat();
	protected Quat totRot = new Quat();
	protected Quat upRot = new Quat();      // pitch
	protected Quat sideRot = new Quat();    // yaw
	protected Quat tilt = new Quat();       // roll

	private boolean allowToggleOrtho = true;
	private boolean allowRotation = true;
	private boolean isOrtho = false;

	private final Mat4 cameraSpaceMatrix = new Mat4();          // World -> View
//	private final Mat4 cameraProjectionMatrix = new Mat4();          // World -> View
//	private final Mat4 inverseCameraSpaceMatrix = new Mat4();          // World -> View
//	private final Mat4 inverseCameraProjectionMatrix = new Mat4();          // World -> View

	private final Mat4 viewMatrix = new Mat4();                  // World -> View
	private final Mat4 projectionMatrix = new Mat4();            // View -> Clip
	private final Mat4 viewProjectionMatrix = new Mat4();        // World -> Clip
	private final Mat4 inverseViewMatrix = new Mat4();           // View -> World
	private final Mat4 inverseProjectionMatrix = new Mat4();     // View -> Clip
	private final Mat4 inverseViewProjectionMatrix = new Mat4(); // Clip -> World


	private final Mat4 tempMat4 = new Mat4();
	private final Mat4 viewPortAntiRotMat = new Mat4();
//	private final Vec3 screenDimension = new Vec3();

	private final Quat inverseCameraRotation = new Quat();
	private final Quat inverseCameraRotXSpinY = new Quat();
	private final Quat inverseCameraRotYSpinY = new Quat();
	private final Quat inverseCameraRotZSpinX = new Quat();
	private final Quat inverseCameraRotZSpinZ = new Quat();

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
//		this.upAngle = (float) Math.toRadians(90 - 34);
		this.upAngle = (float) Math.toRadians(90);
		this.sideAngle = (float) (Math.PI / 2);
		this.distance = 1650;
//		this.distance = 3;
		calculateCameraRotation();
	}

	public CameraManager loadDefaultCameraFor(double boundsRadius){
//		this.upAngle = (float) Math.toRadians(90);
//		this.sideAngle = (float) (Math.PI / 2);
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

	public CameraManager setCamera(RenderNodeCamera cameraNode) {
		return this;
	}
	public CameraManager setCamera(RenderModel renderModel, CameraNode cameraNode) {
		return this;
	}

	public Vec3 getTarget() {
		return target;
	}

	// approximate pixelSize in world length
	public double sizeAdj() {
////		Mat4 invProjectionMat = getInvProjectionMat();
////		vecHeap.set(1.0 / ((float) viewport.getWidth()), 0, 0);
////		vecHeap.transform(invProjectionMat, 1, true);
////		float x = vecHeap.x;
////		vecHeap.set(0,0,0).transform(invProjectionMat, 1, true);
////		return x-vecHeap.x;
////		Mat4 invProjectionMat = getInvProjectionMat();
////		vecHeap.set(1.0 / ((float) viewport.getWidth()), 0, -1);
////		vecHeap.transform(invProjectionMat, 1, true);
////		float x = vecHeap.x;
////		vecHeap.set(0,0,-1).transform(invProjectionMat, 1, true);
////		return x-vecHeap.x;
//		Mat4 invProjectionMat = getInvProjectionMat();
//		vecHeap.set(1.0, 0, -1);
//		vecHeap.transform(invProjectionMat, 1, true);
//		float x = vecHeap.x;
//		vecHeap.set(0,0,-1).transform(invProjectionMat, 1, true);
//		x -= vecHeap.x;
//		return x*2.0/((double) viewport.getWidth());
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
	public void setTargetPoint(float x, float y, float z) {
		target.x = x;
		target.y = y;
		target.z = z;
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
	}

	private void applyPan(double dx, double dy) {
		target.add(getWorldScreenSpaceAsDeltaRay(-dx,-dy));
	}

	public CameraManager setAllowRotation(boolean allowRotation) {
		this.allowRotation = allowRotation;
		return this;
	}

	public void rotate(double right, double up){
		if(allowRotation){
			rot(0, right, -up);
		} else {
			applyPan(right, up);
		}
	}
	public void setCameraRotation(float right, float up) {
		setCameraRotation(right, up, 0);
	}
	public void setCameraRotation(float right, float up, float tilt) {
		if (allowRotation) {
			setRotation(right, up, tilt);
//			System.out.println("rot: " + Math.toDegrees(upAngle) + ", " + Math.toDegrees(sideAngle) + ", " + Math.toDegrees(tiltAngle)
//					+ ", dist: " + distance
//					+ ", target: " + target
//			);
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
//		System.out.println("distance: " + distance);
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
	private Vec3 getWorldScreenSpaceRay(Vec2 view){
		return getWorldScreenSpaceRay(view.x, view.y);
	}
	private Vec3 getWorldScreenSpaceRay(double viewX, double viewY){
		// https://stackoverflow.com/questions/45893277/is-it-possible-get-which-surface-of-cube-will-be-click-in-opengl
		// https://www.3dgep.com/understanding-the-view-matrix/
		// https://gamedev.stackexchange.com/questions/23395/how-to-convert-screen-space-into-3d-world-space
		// https://stackoverflow.com/questions/7692988/opengl-math-projecting-screen-space-to-world-space-coords
		// https://www.tomdalling.com/blog/modern-opengl/explaining-homogenous-coordinates-and-projective-geometry/

		Mat4 invViewProjectionMat = getInvViewProjectionMat();

		// near point
		vecHeap.set(viewX, viewY, -1).transform(invViewProjectionMat, 1, true);
		rayHeap.setPoint(vecHeap);
		// far point
		vecHeap.set(viewX, viewY, 1).transform(invViewProjectionMat, 1, true);
		rayHeap.setDirFromEnd(vecHeap);

		// Calculate the ray-plane intersection point.
		planeHeap.set(camBackward, target);
		float intersectP = planeHeap.getIntersect(rayHeap);

		return new Vec3(rayHeap.getPoint()).addScaled(rayHeap.getDir(), intersectP);
	}
	private Vec3 getWorldScreenSpaceAsDelta(double viewX, double viewY){
		// https://stackoverflow.com/questions/45893277/is-it-possible-get-which-surface-of-cube-will-be-click-in-opengl
		// https://www.3dgep.com/understanding-the-view-matrix/
		// https://gamedev.stackexchange.com/questions/23395/how-to-convert-screen-space-into-3d-world-space
		// https://stackoverflow.com/questions/7692988/opengl-math-projecting-screen-space-to-world-space-coords
		// https://www.tomdalling.com/blog/modern-opengl/explaining-homogenous-coordinates-and-projective-geometry/

		Mat4 invViewProjectionMat = getInvViewProjectionMat();
		Vec3 nearWorldSpaceP = new Vec3(viewX, viewY, -1).transform(invViewProjectionMat, 1, true);
		Vec3 farWorldSpaceP = new Vec3(viewX, viewY, 1).transform(invViewProjectionMat, 1, true);

		// Create a ray from the near clip plane to the far clip plane.
		Vec3 dirP = new Vec3(farWorldSpaceP).sub(nearWorldSpaceP).normalize();

		// Create a ray.
		Vec3 rayPointP = nearWorldSpaceP;

		// Calculate the ray-plane intersection point.
		Vec3 planeNorm = new Vec3(camBackward).normalize();
		System.out.println("planeNorm: " + planeNorm);

		float pD = -target.dot(planeNorm);

		// Calculate distance of intersection point from r.origin.
		float denominatorP = planeNorm.dot(dirP);
		float numeratorP = planeNorm.dot(rayPointP) + pD;
		float tP = -(numeratorP / denominatorP);
		System.out.println("t: " + tP);

		// Calculate the picked position on the y = 0 plane.
		Vec3 point = new Vec3(nearWorldSpaceP).addScaled(dirP,tP);


		// For 0,0:
		Vec3 nearWorldSpaceO = new Vec3(0, 0, -1).transform(invViewProjectionMat, 1, true);
		Vec3 farWorldSpaceO = new Vec3(0, 0, 1).transform(invViewProjectionMat, 1, true);


		// Create a ray from the near clip plane to the far clip plane.
		Vec3 dirO = new Vec3(farWorldSpaceO).sub(nearWorldSpaceO).normalize();

		// Create a ray.
		Vec3 rayPointO = nearWorldSpaceO;

		// Calculate distance of intersection point from r.origin.
		float denominatorO = planeNorm.dot(dirO);
		float numeratorO = planeNorm.dot(rayPointO) + pD;
		float tO = -(numeratorO / denominatorO);
		System.out.println("t: " + tO);

		Vec3 orig = new Vec3(nearWorldSpaceO).addScaled(dirO,tO);

		Vec3 p1 = new Vec3(nearWorldSpaceP);
		Vec3 pO = new Vec3(nearWorldSpaceO);
		Vec3 d1 = new Vec3(dirP).scale(tP);
		Vec3 dO = new Vec3(dirO).scale(tO);

		Vec3 dWorld;

		dWorld = p1.add(d1).sub(pO.add(dO));
		dWorld = p1.add(d1).sub(pO).sub(dO);
		dWorld = p1.sub(pO).add(d1).sub(dO);
		new Vec3(nearWorldSpaceP).addScaled(dirP,tP).sub(new Vec3(nearWorldSpaceO).addScaled(dirO,tO));
		return point.sub(orig);
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
//		Vec3 point = new Vec3(rayHeap.getPoint()).addScaled(rayHeap.getDir(), intersectP);
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


	public ViewBox getViewBox(Vec2 topRight, Vec2 bottomLeft){
		ViewBox viewBox = new ViewBox();
		Vec3 topRightPoint = getWorldScreenSpaceRay(topRight).negate();
		Ray topRightRay = getRayFromScreenSpace(topRight);
		vecHeap.set(topRightRay.getDir()).cross(camRight);
		viewBox.setTop(topRightPoint, vecHeap);
		vecHeap.set(topRightRay.getDir()).cross(camUp).negate();
		viewBox.setRight(topRightPoint, vecHeap);

		Vec3 botLeftPoint = getWorldScreenSpaceRay(bottomLeft).negate();
		Ray botLeftRay = getRayFromScreenSpace(bottomLeft);

		vecHeap.set(botLeftRay.getDir()).cross(camRight).negate();
		viewBox.setBot(botLeftPoint, vecHeap);

		vecHeap.set(botLeftRay.getDir()).cross(camUp);
		viewBox.setLeft(botLeftPoint, vecHeap);
		return viewBox;
	}

	SelectionBoxHelper selectionBoxHelper = new SelectionBoxHelper();
	public SelectionBoxHelper getSelectionBoxHelper(Vec2 topRight, Vec2 bottomLeft){
		planeHeap.set(camBackward, target);
		return selectionBoxHelper.setFrom(topRight, bottomLeft, camRight, camUp, planeHeap, getInvViewProjectionMat());
	}

	public ViewBox getViewBox1(Vec2 topRight, Vec2 bottomLeft){
		ViewBox viewBox = new ViewBox();
		Vec3 topRightPoint = getWorldScreenSpaceRay(topRight);
		Ray topRightRay = getRayFromScreenSpace(topRight);
		vecHeap.set(topRightRay.getDir()).cross(camRight).negate();
		viewBox.setTop(topRightPoint, vecHeap);
		vecHeap.set(topRightRay.getDir()).cross(camUp);
		viewBox.setRight(topRightPoint, vecHeap);

		Vec3 botLeftPoint = getWorldScreenSpaceRay(bottomLeft);
		Ray botLeftRay = getRayFromScreenSpace(bottomLeft);
		vecHeap.set(botLeftRay.getDir()).cross(camRight);
		viewBox.setBot(botLeftPoint, vecHeap);
		vecHeap.set(botLeftRay.getDir()).cross(camUp).negate();
//		vecHeap.set(botLeftRay.getDir()).cross(camUp).negate();
		viewBox.setLeft(botLeftPoint, vecHeap);
		return viewBox;
	}

	public Ray getRayFromScreenSpace(Vec2 view){
		return getRayFromScreenSpace(view.x, view.y);
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

//	public Vec2 getPoint_ifYZplane(double viewX, double viewY) {
////		double x_real = (viewX - (camera.rect.getWidth() / 2.0)) + target.y;
////		double y_real = -(viewY - (camera.rect.getHeight() / 2.0)) + target.z;
//		double x_real = (viewX - (viewport.getWidth() / 2.0)) + target.y;
//		double y_real = -(viewY - (viewport.getHeight() / 2.0)) + target.z;
//		float xRatio = 2.0f * (float) viewX / (float) viewport.getWidth() - 1.0f;
//		float yRatio = 1.0f - 2.0f * (float) viewX / (float) viewport.getHeight();
//
//		Vec2 vec2 = new Vec2(x_real, y_real);
////		vec2.scale((float) (1f / distance));
////		vec2.transform(getViewPortAntiRotMat2());
//
////		System.out.println("CamSpaceMouse: [" + viewX + ", " + viewY + "], " + "zoom: " + m_zoom + ", CamP: " + vec2 + ", camPos: " + cameraPos);
////		Vec2 vec22 = new Vec2(viewX - (viewport.getWidth() / 2.0), -(viewY - (viewport.getHeight() / 2.0)));
////		Vec2 vec22 = new Vec2(viewX*(float)viewport.getWidth(), viewY*(float)viewport.getHeight());
//		Vec2 vec22 = new Vec2(xRatio, yRatio);
//
//		return vec22;
//	}

	public void setViewportCamera(int dist, int side, int height, int rX, int rY, int rZ) {
		setRotation(rY, rZ, rX);

		target.set(dist, side, height);
//		calculateCameraRotation();

	}


	protected void calculateCameraRotation() {
		upRot.setFromAxisAngle(Vec3.Y_AXIS, upAngle);
//		upRot.setFromAxisAngle(Vec3.X_AXIS, verticalAngle);
		sideRot.setFromAxisAngle(Vec3.Z_AXIS, sideAngle);
//		totRot.set(sideRot).mul(upRot);
//		totRot.set(sideRot).mulLeft(upRot);
		tilt.setFromAxisAngle(Vec3.X_AXIS, tiltAngle);
//		totRot.set(upRot).mulLeft(tilt).mulLeft(sideRot);
		totRot.set(tilt).mulLeft(upRot).mulLeft(sideRot);
		inverseCameraRotXSpinY.setFromAxisAngle(Vec3.X_AXIS, sideAngle).normalize();
		inverseCameraRotXSpinY.invertRotation();
		inverseCameraRotYSpinY.setFromAxisAngle(Vec3.Y_AXIS, sideAngle).normalize();
		inverseCameraRotYSpinY.invertRotation();
//		inverseCameraRotZSpinX.setFromAxisAngle(Vec3.Z_AXIS, (float) Math.toRadians(xAngle)).normalize();
		inverseCameraRotZSpinX.setFromAxisAngle(Vec3.Z_AXIS, 0.0f).normalize();
		inverseCameraRotZSpinX.invertRotation();
		inverseCameraRotZSpinZ.setFromAxisAngle(Vec3.Z_AXIS, upAngle).normalize();
		inverseCameraRotZSpinZ.invertRotation();
//		inverseCameraRotation.set(getInverseCameraRotZSpinZ()).mul(getInverseCameraRotYSpinY()).normalize();
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