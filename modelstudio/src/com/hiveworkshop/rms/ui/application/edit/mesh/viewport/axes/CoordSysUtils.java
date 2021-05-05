package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.awt.*;
import java.awt.geom.Point2D;

public class CoordSysUtils {


	public static byte getUnusedXYZ(byte portFirstXYZ, byte portSecondXYZ) {
		if (portFirstXYZ < 0) {
			portFirstXYZ = (byte) (-portFirstXYZ - 1);
		}
		if (portSecondXYZ < 0) {
			portSecondXYZ = (byte) (-portSecondXYZ - 1);
		}
		return (byte) (3 - portFirstXYZ - portSecondXYZ);
	}

	public static Vec3 convertToVec3(CoordinateSystem coordinateSystem, Point point) {
		Vec3 vertex = new Vec3(0, 0, 0);
		vertex.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(point.x));
		vertex.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(point.y));
		return vertex;
	}

	public static Point2D.Double geom(CoordinateSystem coordinateSystem, Point point) {
		return new Point2D.Double(coordinateSystem.geomX(point.x), coordinateSystem.geomY(point.y));
	}

	public static Vec3 convertToVec3(CoordinateSystem coordinateSystem, Vec2 point) {
		Vec3 vertex = new Vec3(0, 0, 0);
		vertex.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(point.x));
		vertex.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(point.y));
		return vertex;
	}

	public static Point2D.Double geom(CoordinateSystem coordinateSystem, Vec2 point) {
		return new Point2D.Double(coordinateSystem.geomX(point.x), coordinateSystem.geomY(point.y));
	}
	public static Vec2 geomV2(CoordinateSystem coordinateSystem, Vec2 point) {
		return new Vec2(coordinateSystem.geomX(point.x), coordinateSystem.geomY(point.y));
	}

	public static Point convertToViewPoint(CoordinateSystem coordinateSystem, Vec3 vertex) {
		int x = (int) (getViewX(coordinateSystem, vertex) + .5);
		int y = (int) (getViewY(coordinateSystem, vertex) + .5);
		return new Point(x, y);
	}

	public static Point convertToViewPoint(CoordinateSystem coordinateSystem, Vec2 vertex) {
		int x = (int) getViewX(coordinateSystem, vertex);
		int y = (int) getViewY(coordinateSystem, vertex);
		return new Point(x, y);
	}

	public static Point convertToViewPoint(CoordinateSystem coordinateSystem, GeosetVertex vertex, RenderModel renderModel) {
//		Vec4 vertexHeap = new Vec4(vertex, 1);

		Vec3 vertexSumHeap = new Vec3(0, 0, 0);
		if (renderModel != null) {
			for (Bone bone : vertex.getBones()) {
				Vec3 appliedVertexHeap = Vec3.getTransformed(vertex, renderModel.getRenderNode(bone).getWorldMatrix());
				vertexSumHeap.add(appliedVertexHeap);
			}
			int boneCount = vertex.getBones().size();
			vertexSumHeap.scale(1f / boneCount);
		}
		int x = (int) getViewX(coordinateSystem, vertexSumHeap);
		int y = (int) getViewY(coordinateSystem, vertexSumHeap);

		return new Point(x, y);
	}
//	public static Point convertToViewPoint(CoordinateSystem coordinateSystem, GeosetVertex vertex, RenderModel renderModel) {
//		Vec4 vertexHeap = new Vec4(vertex, 1);
//
//		Vec4 vertexSumHeap = new Vec4(0, 0, 0, 0);
//		for (Bone bone : vertex.getBones()) {
//			Vec4 appliedVertexHeap = Vec4.getTransformed(vertexHeap, renderModel.getRenderNode(bone).getWorldMatrix());
//			vertexSumHeap.add(appliedVertexHeap);
//		}
//		int boneCount = vertex.getBones().size();
//		vertexSumHeap.scale(1f / boneCount);
//		int x = (int) getViewX(coordinateSystem, vertexSumHeap);
//		int y = (int) getViewY(coordinateSystem, vertexSumHeap);
//
//		return new Point(x, y);
//	}

	public static Vec2 convertToViewVec2(CoordinateSystem coordinateSystem, Vec3 vertex) {
		double x = getViewX(coordinateSystem, vertex);
		double y = getViewY(coordinateSystem, vertex);
		return new Vec2(x, y);
	}

	public static Vec2 convertToViewVec2(CoordinateSystem coordinateSystem, Vec2 vertex) {
		double x = getViewX(coordinateSystem, vertex);
		double y = getViewY(coordinateSystem, vertex);
		return new Vec2(x, y);
	}

	public static Vec2 convertToViewVec2(CoordinateSystem coordinateSystem, GeosetVertex vertex, RenderModel renderModel) {
		Vec4 vertexHeap = new Vec4(vertex, 1);

		Vec4 vertexSumHeap = new Vec4(0, 0, 0, 0);
		for (Bone bone : vertex.getBones()) {
			Vec4 appliedVertexHeap = Vec4.getTransformed(vertexHeap, renderModel.getRenderNode(bone).getWorldMatrix());
			vertexSumHeap.add(appliedVertexHeap);
		}
		int boneCount = vertex.getBones().size();
		vertexSumHeap.scale(1f / boneCount);
		double x = getViewX(coordinateSystem, vertexSumHeap);
		double y = getViewY(coordinateSystem, vertexSumHeap);

		return new Vec2(x, y);
	}

	public static double getViewY(CoordinateSystem coordinateSystem, Vec4 vertexSumHeap) {
		return coordinateSystem.viewY(vertexSumHeap.getCoord(coordinateSystem.getPortSecondXYZ()));
	}

	public static double getViewX(CoordinateSystem coordinateSystem, Vec4 vertexSumHeap) {
		return coordinateSystem.viewX(vertexSumHeap.getCoord(coordinateSystem.getPortFirstXYZ()));
	}

	public static double getViewY(CoordinateSystem coordinateSystem, Vec2 vertex) {
		return coordinateSystem.viewY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
	}

	public static double getViewX(CoordinateSystem coordinateSystem, Vec2 vertex) {
		return coordinateSystem.viewX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
	}

	public static double getViewY(CoordinateSystem coordinateSystem, Vec3 vertex) {
		return coordinateSystem.viewY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
	}

	public static double getViewX(CoordinateSystem coordinateSystem, Vec3 vertex) {
		return coordinateSystem.viewX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
	}
}
