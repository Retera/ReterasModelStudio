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
	double convertX(double x);

	double convertY(double y);

	double geomX(double x);

	double geomY(double y);

	byte getPortFirstXYZ();

	byte getPortSecondXYZ();

	CoordinateSystem copy();

	final class Util {
		private static final class IdentityCoordinateSystem implements CoordinateSystem {
			private final byte b;
			private final byte a;

			private IdentityCoordinateSystem(final byte b, final byte a) {
				this.b = b;
				this.a = a;
			}

			@Override
			public double convertX(final double x) {
				return x;
			}

			@Override
			public double convertY(final double y) {
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

			@Override
			public CoordinateSystem copy() {
				return new IdentityCoordinateSystem(b, a);
			}
		}

		public static CoordinateSystem identity(final byte a, final byte b) {
			return new IdentityCoordinateSystem(b, a);
		}

		public static byte getUnusedXYZ(final CoordinateSystem coordinateSystem) {
			return getUnusedXYZ(coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		}

		public static byte getUnusedXYZ(byte portFirstXYZ, byte portSecondXYZ) {
			if (portFirstXYZ < 0) {
				portFirstXYZ = (byte) (-portFirstXYZ - 1);
			}
			if (portSecondXYZ < 0) {
				portSecondXYZ = (byte) (-portSecondXYZ - 1);
			}
			return (byte) (3 - portFirstXYZ - portSecondXYZ);
		}

		public static double getZoom(final CoordinateSystem coordinateSystem) {
			if (coordinateSystem instanceof Viewport) {
				return ((Viewport) coordinateSystem).getZoom();
			}
			final double originX = coordinateSystem.convertX(0);
			final double offsetX = coordinateSystem.convertX(100);
			return (offsetX - originX) / 100.0;
		}

		public static Point2D.Double geom(final CoordinateSystem coordinateSystem, final Point point) {
			return new Point2D.Double(coordinateSystem.geomX(point.x), coordinateSystem.geomY(point.y));
		}

		public static Vec3 convertToVertex(final CoordinateSystem coordinateSystem, final Point point) {
			final Vec3 vertex = new Vec3(0, 0, 0);
			vertex.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(point.x));
			vertex.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(point.y));
			return vertex;
		}

		public static Point convertToPoint(final CoordinateSystem coordinateSystem, final Vec3 vertex) {
			int x = (int) coordinateSystem.convertX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
			int y = (int) coordinateSystem.convertY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
			return new Point(x, y);
		}

		public static Point convertToPoint(final CoordinateSystem coordinateSystem, final Vec2 vertex) {
			int x = (int) coordinateSystem.convertX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
			int y = (int) coordinateSystem.convertY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
			return new Point(x, y);
		}

		public static Point convertToPoint(final CoordinateSystem coordinateSystem, final GeosetVertex vertex, final RenderModel renderModel) {
			Vec4 vertexHeap = new Vec4(vertex, 1);

			Vec4 vertexSumHeap = new Vec4(0, 0, 0, 0);
			for (final Bone bone : vertex.getBones()) {
				Vec4 appliedVertexHeap = Vec4.getTransformed(vertexHeap, renderModel.getRenderNode(bone).getWorldMatrix());
				vertexSumHeap.add(appliedVertexHeap);
			}
			final int boneCount = vertex.getBones().size();
			vertexSumHeap.scale(1f / boneCount);
			int x = (int) coordinateSystem.convertX(vertexSumHeap.getCoord(coordinateSystem.getPortFirstXYZ()));
			int y = (int) coordinateSystem.convertY(vertexSumHeap.getCoord(coordinateSystem.getPortSecondXYZ()));

			return new Point(x, y);
		}

		private Util() {
		}
	}
}
