package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public class SelectionBoxHelper {
	private final ViewBox viewBox = new ViewBox();
	private final Ray topRightRay = new Ray();
	private final Ray botLeftRay  = new Ray();
	private final Ray topLeftRay = new Ray();
	private final Ray botRightRay  = new Ray();

	private final Vec3 vecHeap = new Vec3();
	private final Vec3 topRightPoint = new Vec3();
	private final Vec3 botLeftPoint = new Vec3();
	private final Vec3 topLeftPoint = new Vec3();
	private final Vec3 botRightPoint = new Vec3();

	private final Ray tempRay = new Ray();
	private final Plane tempPlane = new Plane();


	private final Vec3 v0 = new Vec3();
	private final Vec3 v1 = new Vec3();
	private final Vec3 v2 = new Vec3();

	private final Vec2 temp = new Vec2();

	public SelectionBoxHelper setFrom(Vec2 topRight, Vec2 bottomLeft, Vec3 camRight, Vec3 camUp, Plane viewPlane, Mat4 invViewProjectionMat){
		if (topRight.x == bottomLeft.x) {
			topRight.x +=.00001f;
			bottomLeft.x -=.00001f;
		}
		if (topRight.y == bottomLeft.y) {
			topRight.y +=.00001f;
			bottomLeft.y -=.00001f;
		}

		topRightRay.setNearFar(topRight, invViewProjectionMat);
		botLeftRay.setNearFar(bottomLeft, invViewProjectionMat);
		topLeftRay.setNearFar(temp.set(bottomLeft.x, topRight.y), invViewProjectionMat);
		botRightRay.setNearFar(temp.set(topRight.x, bottomLeft.y), invViewProjectionMat);


		topRightPoint.set(viewPlane.getIntersectP(topRightRay));
		botLeftPoint.set(viewPlane.getIntersectP(botLeftRay));
		topLeftPoint.set(viewPlane.getIntersectP(topLeftRay));
		botRightPoint.set(viewPlane.getIntersectP(botRightRay));

		vecHeap.set(topRightRay.getPoint()).addScaled(topRightRay.getDir(), 50).setAsPlaneNorm(vecHeap, topRightPoint, topLeftPoint).normalize();
		viewBox.setTop(topRightPoint, vecHeap);

		vecHeap.set(topRightRay.getPoint()).addScaled(topRightRay.getDir(), 50).setAsPlaneNorm(vecHeap, botRightPoint, topRightPoint).normalize();
		viewBox.setRight(topRightPoint, vecHeap);

		vecHeap.set(botLeftRay.getPoint()).addScaled(botLeftRay.getDir(), 50).setAsPlaneNorm(vecHeap, botLeftPoint, botRightPoint).normalize();
		viewBox.setBot(botLeftPoint, vecHeap);

		vecHeap.set(botLeftRay.getPoint()).addScaled(botLeftRay.getDir(), 50).setAsPlaneNorm(vecHeap, topLeftPoint, botLeftPoint).normalize();
		viewBox.setLeft(botLeftPoint, vecHeap);

		return this;
	}


	// https://stackoverflow.com/questions/45893277/is-it-possible-get-which-surface-of-cube-will-be-click-in-opengl
	// https://www.3dgep.com/understanding-the-view-matrix/
	// https://gamedev.stackexchange.com/questions/23395/how-to-convert-screen-space-into-3d-world-space
	// https://stackoverflow.com/questions/7692988/opengl-math-projecting-screen-space-to-world-space-coords
	// https://www.tomdalling.com/blog/modern-opengl/explaining-homogenous-coordinates-and-projective-geometry/




	public boolean pointInBox(Vec3 point) {
		return viewBox.pointInBox(point);
	}
	public boolean pointInBox(Vec3 point, float margin) {
		return viewBox.pointInBox(point, margin);
	}
	public boolean anyPointInBox(Vec3... points) {
		for(Vec3 point : points){
			if(viewBox.pointInBox(point)){
				return true;
			}
		}
		return false;
	}
	public boolean anyPointInBox(float margin, Vec3... points) {
		for(Vec3 point : points){
			if(viewBox.pointInBox(point, margin)){
				return true;
			}
		}
		return false;
	}
	public boolean triIntersectBox(Vec3 v0, Vec3 v1, Vec3 v2){
		if(testLine(v0, v1) || testLine(v1, v2) || testLine(v2, v0)){
			return true;
		}

		tempPlane.setFrom(v0, v1, v2);
		return pointInTri(v0, v1, v2, tempPlane.getIntersectP(topRightRay));
	}

	private boolean testLine(Vec3 v0, Vec3 v1){
		float length = v0.distance(v1);
		tempRay.setFromPoints(v0, v1);
		vecHeap.set(tempRay.getPoint()).addScaled(tempRay.getDir(), length);

		float bottomI = viewBox.bottom.getIntersect(tempRay);
		float topI    = viewBox.top.getIntersect(tempRay);
		float rightI  = viewBox.right.getIntersect(tempRay);
		float leftI   = viewBox.left.getIntersect(tempRay);


		if(Float.isFinite(topI) && Float.isFinite(bottomI)
				&& 0 <= topI && topI < length
				&& 0 <= bottomI && bottomI < length){
			vecHeap.set(tempRay.getPoint()).addScaled(tempRay.getDir(), (topI + bottomI)/2f);

			if(pointInBox(vecHeap)){
				return true;
			}
		}
		if(Float.isFinite(rightI) && Float.isFinite(leftI)
				&& 0 <= rightI && rightI<length
				&& 0 <= leftI && leftI<length){
			vecHeap.set(tempRay.getPoint()).addScaled(tempRay.getDir(), (rightI + leftI)/2f);
			return pointInBox(vecHeap);
		}

		return false;
	}

	private boolean pointInTri(Vec3 A, Vec3 B, Vec3 C, Vec3 P){
		// https://blackpawn.com/texts/pointinpoly/
		// Compute vectors
		v0.set(C).sub(A);
		v1.set(B).sub(A);
		v2.set(P).sub(A);

		// Compute dot products
		float dot00 = v0.dot(v0);
		float dot01 = v0.dot(v1);
		float dot02 = v0.dot(v2);
		float dot11 = v1.dot(v1);
		float dot12 = v1.dot(v2);

		// Compute barycentric coordinates
		float invDenom = 1.0f / (dot00 * dot11 - dot01 * dot01);
		float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
		float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

		return (u >= 0) && (v >= 0) && (u + v < 1);
	}
}
