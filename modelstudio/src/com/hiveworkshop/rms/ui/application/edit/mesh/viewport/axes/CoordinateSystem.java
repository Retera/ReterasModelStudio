package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes;

import java.awt.Point;
import java.awt.geom.Point2D;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

public interface CoordinateSystem extends CoordinateAxes {
	double convertX(double x);

	double convertY(double y);

	double geomX(double x);

	double geomY(double y);

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

		public static byte getUnusedXYZ(final CoordinateAxes coordinateSystem) {
			return getUnusedXYZ(coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ());
		}

		public static byte getUnusedXYZ(final byte portFirstXYZ, final byte portSecondXYZ) {
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
			return convertToVertex(coordinateSystem, point, vertex);
		}

		public static Vec3 convertToVertex(final CoordinateSystem coordinateSystem, final Point point,
				final Vec3 recycleVertex) {
			recycleVertex.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(point.x));
			recycleVertex.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(point.y));
			return recycleVertex;
		}

		public static Point convertToPoint(final CoordinateSystem coordinateSystem, final Vec3 vertex) {
			return convertToPoint(coordinateSystem, vertex, new Point(0, 0));
		}

		public static Point convertToPoint(final CoordinateSystem coordinateSystem, final Vec3 vertex,
				final Point recyclePoint) {
			recyclePoint.x = (int) coordinateSystem.convertX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
			recyclePoint.y = (int) coordinateSystem.convertY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
			return recyclePoint;
		}

		public static Point convertToPoint(final CoordinateSystem coordinateSystem, final Vec2 vertex,
				final Point recyclePoint) {
			recyclePoint.x = (int) coordinateSystem.convertX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
			recyclePoint.y = (int) coordinateSystem.convertY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
			return recyclePoint;
		}

		private static final Vec4 vertexHeap = new Vec4();
		private static final Vec4 appliedVertexHeap = new Vec4();
		private static final Vec4 vertexSumHeap = new Vec4();

		public static Point convertToPoint(final CoordinateSystem coordinateSystem, final GeosetVertex vertex,
				final Point recyclePoint, final RenderModel renderModel) {
			vertexHeap.x = vertex.x;
			vertexHeap.y = vertex.y;
			vertexHeap.z = vertex.z;
			vertexHeap.w = 1;
			vertexSumHeap.set(0, 0, 0, 0);
			for (final Bone bone : vertex.getBones()) {
				renderModel.getRenderNode(bone).getWorldMatrix().transform(vertexHeap, appliedVertexHeap);
				vertexSumHeap.add(appliedVertexHeap);
			}
			final int boneCount = vertex.getBones().size();
			vertexSumHeap.x /= boneCount;
			vertexSumHeap.y /= boneCount;
			vertexSumHeap.z /= boneCount;
			vertexSumHeap.w /= boneCount;
			recyclePoint.x = (int) coordinateSystem
					.convertX(vertexSumHeap.getCoord(coordinateSystem.getPortFirstXYZ()));
			recyclePoint.y = (int) coordinateSystem
					.convertY(vertexSumHeap.getCoord(coordinateSystem.getPortSecondXYZ()));
			return recyclePoint;
		}

		private Util() {
		}
	}
}
