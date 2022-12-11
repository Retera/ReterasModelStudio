package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes;

import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.Plane;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.SelectionBoxHelper;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;

public final class CoordinateSystem extends AbstractCamera {
	private final JComponent parent;

	private final Mat4 cameraSpaceMatrix = new Mat4();          // World -> Camera
	private final Mat4 viewMatrix = new Mat4();                  // World -> View
	private final Mat4 projectionMatrix = new Mat4();            // View -> Clip
	private final Mat4 viewProjectionMatrix = new Mat4();        // World -> Clip
	private final Mat4 invViewProjectionMatrix = new Mat4();     // Clip -> World

	private final Vec3 target = new Vec3();
	private final Vec3 cameraRight = new Vec3();
	private final Vec3 cameraUp = new Vec3();
	private final Vec3 camPosition = new Vec3();
	private final Vec3 camBackward = new Vec3();

	private final Vec3 vecHeap = new Vec3();
	private final Mat4 tempMat4 = new Mat4();

	private final double ZOOM_FACTOR = 1.15;
	private final Vec2 temp = new Vec2();
	private final Vec2 temp2 = new Vec2();
	private final Vec3 tempV3 = new Vec3();

	private double cameraX = 0;
	private double cameraY = 0;

	private double zoom = 1;
	private double imageAspectRatio = 1;

	public CoordinateSystem(Vec3 right, Vec3 up, JComponent parent) {
		this.parent = parent;
		setDimensions(right, up);
	}

	public CoordinateSystem setDimensions(Vec3 right, Vec3 up) {

		cameraRight.set(right);
		cameraUp.set(up);
		camBackward.set(cameraRight).cross(cameraUp).normalize();

		updateCamera();
		return this;
	}

	public CoordinateSystem setImageAspectRatio(double imageAspectRatio) {
		this.imageAspectRatio = imageAspectRatio;
		updateCamera();
		return this;
	}

	SelectionBoxHelper selectionBoxHelper = new SelectionBoxHelper();
	private final Plane planeHeap = new Plane();
	public SelectionBoxHelper getSelectionBoxHelper(Vec2 topRight, Vec2 bottomLeft){
		planeHeap.set(camBackward, target);
		return selectionBoxHelper.setFrom(topRight, bottomLeft, cameraRight, cameraUp, planeHeap, invViewProjectionMatrix);
	}

	// 1 px on screen to distance in world
	public double sizeAdj(){
		double x1 = geomV(1, 0).x;
		double x2 = geomV(0, 0).x;
		return x1-x2;
	}

	public double getImageAspectRatio() {
		return imageAspectRatio;
	}
	public double getCameraX() {
		return cameraX;
	}
	public double getCameraY() {
		return cameraY;
	}

	public int getParentHeight() {
		if(parent != null){
			return parent.getHeight();
		}
		return 480;
	}
	public int getParentWidth() {
		if(parent != null){
			return parent.getWidth();
		}
		return 640;
	}
	public Component getParent() {
		return parent;
	}
	public Component getComponent() {
		return parent;
	}

	public double getZoom() {
		return zoom;
	}

	public CoordinateSystem setZoom(double zoom) {
		this.zoom = zoom;
		updateCamera();
		return this;
	}

	public void doZoom(MouseWheelEvent e) {
		doZoom(e.getWheelRotation(),e.getX(), e.getY());
	}

	public CoordinateSystem doZoom(int wr, double mouseX, double mouseY) {
		int dir = wr < 0 ? -1 : 1;
		double w = mouseX - (getParentWidth() / 2.0);
		double h = mouseY - (getParentHeight() / 2.0);

		for (int i = 0; i < wr * dir; i++) {
			double zoomAdjust = (ZOOM_FACTOR - 1) * dir / zoom;

			if (dir == -1) {
				cameraX -= w * zoomAdjust / ZOOM_FACTOR / imageAspectRatio;
				cameraY += h * zoomAdjust / ZOOM_FACTOR;
				zoom *= ZOOM_FACTOR;
			} else {
				cameraX -= w * zoomAdjust  / imageAspectRatio;
				cameraY += h * zoomAdjust ;
				zoom /= ZOOM_FACTOR;
			}
		}
		updateCamera();
		return this;
	}

	public CoordinateSystem zoomIn(double amount) {
		zoom *= amount;
		updateCamera();
		return this;
	}

	public CoordinateSystem zoomOut(double amount) {
		zoom /= amount;
		updateCamera();
		return this;
	}

	public CoordinateSystem setPosition(double x, double y) {
		cameraX = x;
		cameraY = y;
		updateCamera();
		return this;
	}

	public void rotate(double right, double up){
		translateZoomed(right, up);
	}

	public void translate(double x, double y) {
		cameraX -= x / imageAspectRatio;
		cameraY -= y;
		updateCamera();
	}

	public CoordinateSystem translateZoomed(double x, double y) {
		cameraX -= .5 * x * getParentWidth() / zoom / imageAspectRatio;
		cameraY -= .5 * y * getParentHeight()  / zoom;

		updateCamera();
		return this;
	}

	// Geometry to screen (pixel coordinates in parent component)
	public Vec2 viewV(Vec3 vertex) {
		return viewV(vertex.x, vertex.y, vertex.z);
	}
	public Vec2 viewV(Vec2 vec2) {
		return viewV(vec2.x, vec2.y, 0);
	}
	public Vec2 viewV(double x, double y) {
		return viewV(x, y, 0);
	}
	public Vec2 viewV(double x, double y, double z) {
		tempV3.set(x, y, z).transform(viewProjectionMatrix, 1, true);
		return new Vec2((1+tempV3.x)/2.0 * getParentWidth(), (1-tempV3.y)/2.0 * getParentHeight());
	}

	// Geometry to clip (normalized view space ([-1,1] to [1,-1] corresponding to [0,0] to [width, height] in parent component)
	public Vec2 viewVN(Vec3 vertex) {
		return viewVN(vertex.x, vertex.y, vertex.z);
	}
	public Vec2 viewVN(Vec2 vec2) {
		return viewVN(vec2.x, vec2.y, 0);
	}
	public Vec2 viewVN(double x, double y) {
		return viewVN(x, y, 0);
	}
	public Vec2 viewVN(double x, double y, double z) {
		tempV3.set(x, y, z).transform(viewProjectionMatrix, 1, true);
		return new Vec2(tempV3.x, tempV3.y);
	}

	// Screen to geometry coordinates
	public Vec2 geomV(Vec2 vec2) {
		return geomV(vec2.x, vec2.y);
	}
	public Vec2 geomV(double x, double y) {
		return geomVN(
				x * (2.0/(double) getParentWidth())-1,
				1 - y *(2.0/(double) getParentHeight()));
	}

	// Clip space to geometry coordinates
	public Vec2 geomVN(Vec2 vec2) {
		return geomVN(vec2.x, vec2.y);
	}
	public Vec2 geomVN(double x, double y) {
		tempV3.set(x, y, 0).transform(invViewProjectionMatrix, 1, true);
		return temp.set(tempV3.x, tempV3.y);
	}
	public Vec3 getGeoPoint(Vec2 vec2) {
		return getGeoPoint(vec2.x, vec2.y);
	}
	public Vec3 getGeoPoint(double x, double y) {
		return tempV3.set(x, y, 0).transform(invViewProjectionMatrix, 1, true);
	}

	public Vec3 getCamRight() {
		return camBackward;
	}
	public Vec3 getCamUp() {
		return camBackward;
	}
	public Vec3 getCamBackward() {
		return camBackward;
	}

	public Mat4 getInvViewProjectionMat() {
		return invViewProjectionMatrix;
	}
	public Mat4 getViewProjectionMatrix() {
		return viewProjectionMatrix;
	}

	public void updateCamera() {
		target.set(Vec3.ZERO).addScaled(cameraRight, (float) cameraX).addScaled(cameraUp, (float) cameraY);

		cameraSpaceMatrix.set(cameraRight, cameraUp, camBackward);

		camPosition.set(camBackward).add(target);


		vecHeap.set(camPosition).negate();
		tempMat4.setIdentity().translate(vecHeap);

		viewMatrix.set(cameraSpaceMatrix).mul(tempMat4);

		float distance = (float) (1f/zoom);
		float aspectRatioF = (float) imageAspectRatio;
		float halfDist = distance / 2.0f;
		projectionMatrix.setOrtho(
				-getParentWidth()* halfDist /aspectRatioF, getParentWidth()* halfDist /aspectRatioF,
				-getParentHeight()* halfDist, getParentHeight()* halfDist,
				-1000, 1000);
		viewProjectionMatrix.set(projectionMatrix).mul(viewMatrix);
		invViewProjectionMatrix.set(viewProjectionMatrix).invert();
	}
}
