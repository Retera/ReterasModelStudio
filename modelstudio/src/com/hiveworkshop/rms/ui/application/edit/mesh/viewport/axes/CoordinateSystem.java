package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.awt.*;
import java.awt.geom.Point2D;

public interface CoordinateSystem {

	double viewX(double x);

	double viewY(double y);

	double geomX(double x);

	double geomY(double y);

	byte getPortFirstXYZ();

	byte getPortSecondXYZ();

//	CoordinateSystem copy();


	static double getZoom(final CoordinateSystem coordinateSystem) {
		if (coordinateSystem instanceof Viewport) {
			return ((Viewport) coordinateSystem).getZoom();
		}
		final double originX = coordinateSystem.viewX(0);
		final double offsetX = coordinateSystem.viewX(100);
		return (offsetX - originX) / 100.0;
	}

	static CoordinateSystem identity(final byte a, final byte b) {
		return new IdentityCoordinateSystem(b, a);
	}

	static byte getUnusedXYZ(final CoordinateSystem coordinateSystem) {
		return getUnusedXYZ(coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
	}

	static byte getUnusedXYZ(byte portFirstXYZ, byte portSecondXYZ) {
		if (portFirstXYZ < 0) {
			portFirstXYZ = (byte) (-portFirstXYZ - 1);
		}
		if (portSecondXYZ < 0) {
			portSecondXYZ = (byte) (-portSecondXYZ - 1);
		}
		return (byte) (3 - portFirstXYZ - portSecondXYZ);
	}

	static Vec3 convertToVec3(final CoordinateSystem coordinateSystem, final Point point) {
		final Vec3 vertex = new Vec3(0, 0, 0);
		vertex.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(point.x));
		vertex.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(point.y));
		return vertex;
	}

	static Point2D.Double geom(final CoordinateSystem coordinateSystem, final Point point) {
		return new Point2D.Double(coordinateSystem.geomX(point.x), coordinateSystem.geomY(point.y));
	}

	static Vec3 convertToVec3(final CoordinateSystem coordinateSystem, final Vec2 point) {
		final Vec3 vertex = new Vec3(0, 0, 0);
		vertex.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(point.x));
		vertex.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(point.y));
		return vertex;
	}

	static Point2D.Double geom(final CoordinateSystem coordinateSystem, final Vec2 point) {
		return new Point2D.Double(coordinateSystem.geomX(point.x), coordinateSystem.geomY(point.y));
	}

	static Point convertToViewPoint(final CoordinateSystem coordinateSystem, final Vec3 vertex) {
		int x = (int) (getViewX(coordinateSystem, vertex) + .5);
		int y = (int) (getViewY(coordinateSystem, vertex) + .5);
		return new Point(x, y);
	}

	static Point convertToViewPoint(final CoordinateSystem coordinateSystem, final Vec2 vertex) {
		int x = (int) getViewX(coordinateSystem, vertex);
		int y = (int) getViewY(coordinateSystem, vertex);
		return new Point(x, y);
	}

	static Point convertToViewPoint(final CoordinateSystem coordinateSystem, final GeosetVertex vertex, final RenderModel renderModel) {
		Vec4 vertexHeap = new Vec4(vertex, 1);

		Vec4 vertexSumHeap = new Vec4(0, 0, 0, 0);
		for (final Bone bone : vertex.getBones()) {
			Vec4 appliedVertexHeap = Vec4.getTransformed(vertexHeap, renderModel.getRenderNode(bone).getWorldMatrix());
			vertexSumHeap.add(appliedVertexHeap);
		}
		final int boneCount = vertex.getBones().size();
		vertexSumHeap.scale(1f / boneCount);
		int x = (int) getViewX(coordinateSystem, vertexSumHeap);
		int y = (int) getViewY(coordinateSystem, vertexSumHeap);

		return new Point(x, y);
	}

	static Vec2 convertToViewVec2(final CoordinateSystem coordinateSystem, final Vec3 vertex) {
		double x = getViewX(coordinateSystem, vertex);
		double y = getViewY(coordinateSystem, vertex);
		return new Vec2(x, y);
	}

	static Vec2 convertToViewVec2(final CoordinateSystem coordinateSystem, final Vec2 vertex) {
		double x = getViewX(coordinateSystem, vertex);
		double y = getViewY(coordinateSystem, vertex);
		return new Vec2(x, y);
	}

	static Vec2 convertToViewVec2(final CoordinateSystem coordinateSystem, final GeosetVertex vertex, final RenderModel renderModel) {
		Vec4 vertexHeap = new Vec4(vertex, 1);

		Vec4 vertexSumHeap = new Vec4(0, 0, 0, 0);
		for (final Bone bone : vertex.getBones()) {
			Vec4 appliedVertexHeap = Vec4.getTransformed(vertexHeap, renderModel.getRenderNode(bone).getWorldMatrix());
			vertexSumHeap.add(appliedVertexHeap);
		}
		final int boneCount = vertex.getBones().size();
		vertexSumHeap.scale(1f / boneCount);
		double x = getViewX(coordinateSystem, vertexSumHeap);
		double y = getViewY(coordinateSystem, vertexSumHeap);

		return new Vec2(x, y);
	}


	static double getViewY(CoordinateSystem coordinateSystem, Vec4 vertexSumHeap) {
		return coordinateSystem.viewY(vertexSumHeap.getCoord(coordinateSystem.getPortSecondXYZ()));
	}

	static double getViewX(CoordinateSystem coordinateSystem, Vec4 vertexSumHeap) {
		return coordinateSystem.viewX(vertexSumHeap.getCoord(coordinateSystem.getPortFirstXYZ()));
	}

	static double getViewY(CoordinateSystem coordinateSystem, Vec2 vertex) {
		return coordinateSystem.viewY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
	}

	static double getViewX(CoordinateSystem coordinateSystem, Vec2 vertex) {
		return coordinateSystem.viewX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
	}

	static double getViewY(CoordinateSystem coordinateSystem, Vec3 vertex) {
		return coordinateSystem.viewY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
	}

	static double getViewX(CoordinateSystem coordinateSystem, Vec3 vertex) {
		return coordinateSystem.viewX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
	}

	final class IdentityCoordinateSystem implements CoordinateSystem {
		private final byte b;
		private final byte a;

		private IdentityCoordinateSystem(final byte b, final byte a) {
			this.b = b;
			this.a = a;
		}

		@Override
		public double viewX(final double x) {
			return x;
		}

		@Override
		public double viewY(final double y) {
			return y;
		}

		@Override
		public double geomX(final double x) {
			return x;
		}

		@Override
		public double geomY(final double y) {
			return y;
		}

		@Override
		public byte getPortFirstXYZ() {
			return a;
		}

		@Override
		public byte getPortSecondXYZ() {
			return b;
		}

//		@Override
//		public CoordinateSystem copy() {
//			return new IdentityCoordinateSystem(b, a);
//		}
	}
}
