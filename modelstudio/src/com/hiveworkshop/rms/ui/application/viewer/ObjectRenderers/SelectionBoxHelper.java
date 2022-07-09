package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public class SelectionBoxHelper {
	ViewBox viewBox = new ViewBox();
	Ray topRightRay = new Ray();
	Ray botLeftRay  = new Ray();

	protected Vec3 vecHeap = new Vec3();
//	private final Ray rayHeap = new Ray();
//	private final Plane viewPlane = new Plane();
	Vec3 topRightPoint = new Vec3();
	Vec3 botLeftPoint = new Vec3();

	public SelectionBoxHelper setFrom(Vec2 topRight, Vec2 bottomLeft, Vec3 camRight, Vec3 camUp, Plane viewPlane, Mat4 invViewProjectionMat){
		topRightRay.setNearFar(topRight, invViewProjectionMat);
		topRightPoint.set(topRightRay.getPoint()).addScaled(topRightRay.getDir(), viewPlane.getIntersect3(topRightRay)).negate();

		vecHeap.set(topRightRay.getDir()).cross(camRight);
		viewBox.setTop(topRightPoint, vecHeap);

		vecHeap.set(topRightRay.getDir()).cross(camUp).negate();
		viewBox.setRight(topRightPoint, vecHeap);

		botLeftRay.setNearFar(bottomLeft, invViewProjectionMat);
		botLeftPoint.set(botLeftRay.getPoint()).addScaled(botLeftRay.getDir(), viewPlane.getIntersect3(botLeftRay)).negate();

		vecHeap.set(botLeftRay.getDir()).cross(camRight).negate();
		viewBox.setBot(botLeftPoint, vecHeap);

		vecHeap.set(botLeftRay.getDir()).cross(camUp);
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
	public boolean anyPointInBox(Vec3... points) {
		for(Vec3 point : points){
			if(viewBox.pointInBox(point)){
				return true;
			}
		}
		return false;
	}

	private Ray tempRay = new Ray();
	private Vec3 tempVec = new Vec3();
	private Plane tempPlane = new Plane();

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
//		tempVec.set(v1).sub(tempRay.getPoint());
		tempVec.set(tempRay.getPoint()).addScaled(tempRay.getDir(), length);
//		System.out.println( "l: " + length + ", vecs: " + v1 + " vs " + tempVec);


		float bottomI = viewBox.bottom.getIntersect3(tempRay);
		float topI = viewBox.top.getIntersect3(tempRay);
		float rightI = viewBox.right.getIntersect3(tempRay);
		float leftI = viewBox.left.getIntersect3(tempRay);

//		System.out.println(
//				"botI: " + bottomI +
//						"\ntopI: " + topI +
//						"\nrightI: " + rightI +
//						"\nleftI:  " + leftI
//		);

		boolean b = false;

		if(Float.isFinite(topI) && Float.isFinite(bottomI)
				&& 0 <= topI && topI<length
				&& 0 <= bottomI && bottomI<length){
			tempVec.set(tempRay.getPoint()).addScaled(tempRay.getDir(), (topI + bottomI)/2f);
//			System.out.println("topBot vec: " + tempVec);
			b = pointInBox(tempVec);
//			if(pointInBox(tempVec)){
//				return true;
//			}
		}
		if(Float.isFinite(rightI) && Float.isFinite(leftI)
				&& 0 <= rightI && rightI<length
				&& 0 <= leftI && leftI<length){
			tempVec.set(tempRay.getPoint()).addScaled(tempRay.getDir(), (rightI + leftI)/2f);
//			System.out.println("leftRight vec: " + tempVec);
			b = b || pointInBox(tempVec);
//			if(pointInBox(tempVec)){
//				return true;
//			}
		}

		return b;
	}

	Vec3 v0 = new Vec3();
	Vec3 v1 = new Vec3();
	Vec3 v2 = new Vec3();

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
		float invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
		float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
		float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

		// Check if point is in triangle
		return (u >= 0) && (v >= 0) && (u + v < 1);
	}
}
