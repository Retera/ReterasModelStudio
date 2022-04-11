package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public class HitTestStuff {
	public static boolean hitTest(Vec2 min, Vec2 max, Vec3 vec3, CoordinateSystem coordinateSystem, double vertexSize) {
		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();

		Vec2 vertexV2 = vec3.getProjected(dim1, dim2);

		double vertSize = vertexSize / 2.0 / coordinateSystem.getZoom();
		return hitTest(min, max, vertexV2, vertSize);
	}

	public static boolean hitTest(Vec2 min, Vec2 max, Vec3 vec3, Mat4 viewPortMat, double vertexSize) {
		Vec3 viewPAdj = new Vec3(vec3).transform(viewPortMat);
		Vec2 vertexV2 = viewPAdj.getProjected((byte) 1, (byte) 2);

		return hitTest(min, max, vertexV2, vertexSize);
	}

	public static boolean hitTest(Vec3 vec3, Vec2 point, Mat4 viewPortAntiRotMat, double vertexSize) {
		Vec3 viewPAdj = new Vec3(vec3).transform(viewPortAntiRotMat);
		Vec2 vertexV2 = viewPAdj.getProjected((byte) 1, (byte) 2);

		//		System.out.println(vertSize + " >= " + vertexV2.distance(point) + " (" + vertexV2 + " to " + point + ") vertexSize:" + vertexSize);
		return vertexV2.distance(point) <= vertexSize;
	}

	public static boolean triHitTest(Triangle triangle, Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();

		Vec2[] triPoints = triangle.getProjectedVerts(dim1, dim2);
		return triangleOverlapArea(min, max, triPoints);
	}

	public static boolean triHitTest(Triangle triangle, Vec2 min, Vec2 max, int tvIndex) {
		Vec2[] tVerts = triangle.getTVerts(tvIndex);

		return triangleOverlapArea(min, max, tVerts);
	}

	public static boolean triHitTest(Triangle triangle, Vec2 min, Vec2 max, int tvIndex, Mat4 viewPortAntiRotMat) {
		Vec2[] tVerts = triangle.getTVerts(tvIndex);
		Vec2[] tVerts2 = new Vec2[tVerts.length];

		for (int i = 0; i < tVerts.length; i++) {
			tVerts2[i] = new Vec2(tVerts[i]).transform(viewPortAntiRotMat);
		}


		return triangleOverlapArea(min, max, tVerts2);
	}

	public static boolean triHitTest(Triangle triangle, Vec2 min, Vec2 max, Mat4 viewPortMat) {
		Vec2[] triPoints = new Vec2[] {
				new Vec3(triangle.get(0)).transform(viewPortMat).getProjected((byte) 1, (byte) 2),
				new Vec3(triangle.get(1)).transform(viewPortMat).getProjected((byte) 1, (byte) 2),
				new Vec3(triangle.get(2)).transform(viewPortMat).getProjected((byte) 1, (byte) 2),
		};

		return triangleOverlapArea(min, max, triPoints);
	}

	private static boolean triangleOverlapArea(Vec2 min, Vec2 max, Vec2[] triPoints) {
		Vec2 corner1 = new Vec2(min.x, max.y); // not sure if these are needed...
		Vec2 corner2 = new Vec2(max.x, min.y);

		return isAnyTriPointWithinArea(triPoints, min, max)
				|| isAnyPointInTriangle(triPoints, min, max, corner1, corner2)
				|| triEdgeIntersectsLine(triPoints, min, max);
	}

	public static boolean triHitTest(Triangle triangle, Vec2 point, CoordinateSystem coordinateSystem) {
		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();

		Vec2[] triPoints = triangle.getProjectedVerts(dim1, dim2);

		return pointInTriangle(point, triPoints);
	}

	public static boolean triHitTest(Triangle triangle, Vec2 point, Mat4 viewPortMat) {
		Vec2[] triPoints = new Vec2[] {
				new Vec3(triangle.get(0)).transform(viewPortMat).getProjected((byte) 1, (byte) 2),
				new Vec3(triangle.get(1)).transform(viewPortMat).getProjected((byte) 1, (byte) 2),
				new Vec3(triangle.get(2)).transform(viewPortMat).getProjected((byte) 1, (byte) 2),
		};

		return pointInTriangle(point, triPoints);
	}

	//ugg
	public static boolean triHitTest(Triangle triangle, Vec2 point, int tvIndex) {
		return pointInTriangle(point, triangle.getTVerts(tvIndex));
	}

	public static boolean hitTest(Vec2 min, Vec2 max, Vec2 point, double vertexSize) {
		return point.distance(min) <= vertexSize
				|| point.distance(max) <= vertexSize
				|| within(point, min, max);
	}

	public static boolean hitTest(Vec2 vertex, Vec2 point, double vertexSize) {
		return vertex.distance(point) <= vertexSize;
	}

	private static boolean isAnyPointInTriangle(Vec2[] triPoints, Vec2... points) {
		for (Vec2 point : points) {
			if (pointInTriangle(point, triPoints)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isAnyTriPointWithinArea(Vec2[] triPoints, Vec2 min, Vec2 max) {
		return within(triPoints[0], min, max)
				|| within(triPoints[1], min, max)
				|| within(triPoints[2], min, max);
	}

	private static boolean within(Vec2 point, Vec2 min, Vec2 max) {
		boolean xIn = max.x >= point.x && point.x >= min.x;
		boolean yIn = max.y >= point.y && point.y >= min.y;
		return xIn && yIn;
	}

	private static boolean within(Vec3 point, Vec3 min, Vec3 max) {
		boolean xIn = max.x >= point.x && point.x >= min.x;
		boolean yIn = max.y >= point.y && point.y >= min.y;
		boolean zIn = max.z >= point.z && point.z >= min.z;
		return xIn && yIn && zIn;
	}

	private static boolean pointInTriangle(Vec2 point, Vec2[] triPoints) {
		return pointInTriangle(point, triPoints[0], triPoints[1], triPoints[2]);
	}

	private static boolean pointInTriangle(Vec2 point, Vec2 v1, Vec2 v2, Vec2 v3) {
		float d1 = (point.x - v2.x) * (v1.y - v2.y) - (v1.x - v2.x) * (point.y - v2.y);
		float d2 = (point.x - v3.x) * (v2.y - v3.y) - (v2.x - v3.x) * (point.y - v3.y);
		float d3 = (point.x - v1.x) * (v3.y - v1.y) - (v3.x - v1.x) * (point.y - v1.y);
//        float d1 = sign(point, v1, v2);
//        float d2 = sign(point, v2, v3);
//        float d3 = sign(point, v3, v1);

		boolean has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
		boolean has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

		return !(has_neg && has_pos);
	}

	private static boolean pointInTriangle2(Vec2 point, Vec2[] triP) {
		float d1 = (point.x - triP[1].x) * (triP[0].y - triP[1].y) - (triP[0].x - triP[1].x) * (point.y - triP[1].y);
		float d2 = (point.x - triP[2].x) * (triP[1].y - triP[2].y) - (triP[1].x - triP[2].x) * (point.y - triP[2].y);
		float d3 = (point.x - triP[0].x) * (triP[2].y - triP[0].y) - (triP[2].x - triP[0].x) * (point.y - triP[0].y);
//        float d1 = sign(point, triP[0], triP[1]);
//        float d2 = sign(point, triP[1], triP[2]);
//        float d3 = sign(point, triP[2], triP[0]);

		boolean has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
		boolean has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

		return !(has_neg && has_pos);
	}

	public static boolean triEdgeIntersectsLine(Vec2 min, Vec2 max, Vec2 p1, Vec2 p2) {
		float sel_K = (max.y - min.y) / (max.x - min.x);
		float sel_M = max.y - max.x * sel_K;

		float line_K = (p1.y - p2.y) / (p1.x - p2.x);
		float line1_M = p1.y - p1.x * line_K;

		if (line_K == sel_K) {
			return sel_M == line1_M;
		}

		float x_p = (line1_M - sel_M) / (sel_K - line_K);
		float y_p = x_p * sel_K + sel_M;

		return min.x <= x_p && x_p <= max.x && min.y <= y_p && y_p <= max.y;
	}

	public static boolean triEdgeIntersectsLine(Vec2[] points, Vec2 min, Vec2 max) {
		float sel_K = (max.y - min.y) / (max.x - min.x);
		float sel_M = max.y - max.x * sel_K;

		for (int i = 0; i < 3; i++) {
			Vec2 p1 = points[i % 3];
			Vec2 p2 = points[(i + 1) % 3];

			float line_K = (p1.y - p2.y) / (p1.x - p2.x);
			float line1_M = p1.y - p1.x * line_K;

			if (line_K == sel_K) {
				if (sel_M == line1_M) {
					System.out.println("paralell lines");
					return true;
				}
			} else {

				float x_p = (line1_M - sel_M) / (sel_K - line_K);
				float y_p = x_p * sel_K + sel_M;

				if (isBetween(x_p, min.x, max.x) && isBetween(y_p, min.y, max.y)
						&& isBetween(x_p, p1.x, p2.x) && isBetween(y_p, p1.y, p2.y)) {
//					System.out.println("intersecting at: " + x_p + ", " + y_p + " (min: " + min + " max: " + max + " p1: " + p1 + " p2: " + p2);
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isBetween(float value, float endP1, float endP2) {
		float min = Math.min(endP1, endP2);
		float max = Math.max(endP1, endP2);
		return min <= value && value <= max;
	}
}
