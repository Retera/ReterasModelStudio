package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.GeosetVertexBoneLink;
import com.hiveworkshop.wc3.mdl.TVertex;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.render3d.RenderModel;

public interface CoordinateSystem extends CoordinateAxes {
	double convertX(double x);

	double convertY(double y);

	double geomX(double x);

	double geomY(double y);

	CoordinateSystem copy();

	public final class Util {
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

		public static Vertex convertToVertex(final CoordinateSystem coordinateSystem, final Point point) {
			final Vertex vertex = new Vertex(0, 0, 0);
			return convertToVertex(coordinateSystem, point, vertex);
		}

		public static Vertex convertToVertex(final CoordinateSystem coordinateSystem, final Point point,
				final Vertex recycleVertex) {
			recycleVertex.setCoord(coordinateSystem.getPortFirstXYZ(), coordinateSystem.geomX(point.x));
			recycleVertex.setCoord(coordinateSystem.getPortSecondXYZ(), coordinateSystem.geomY(point.y));
			return recycleVertex;
		}

		public static Point convertToPoint(final CoordinateSystem coordinateSystem, final Vertex vertex) {
			return convertToPoint(coordinateSystem, vertex, new Point(0, 0));
		}

		public static Point convertToPoint(final CoordinateSystem coordinateSystem, final Vertex vertex,
				final Point recyclePoint) {
			recyclePoint.x = (int) coordinateSystem.convertX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
			recyclePoint.y = (int) coordinateSystem.convertY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
			return recyclePoint;
		}

		public static Point convertToPoint(final CoordinateSystem coordinateSystem, final TVertex vertex,
				final Point recyclePoint) {
			recyclePoint.x = (int) coordinateSystem.convertX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
			recyclePoint.y = (int) coordinateSystem.convertY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
			return recyclePoint;
		}

		private static final Vector4f vertexHeap = new Vector4f();
		private static final Vector4f appliedVertexHeap = new Vector4f();
		private static final Vector4f vertexSumHeap = new Vector4f();
		private static final Matrix4f skinBonesMatrixSumHeap = new Matrix4f();

		public static Point convertToPoint(final CoordinateSystem coordinateSystem, final GeosetVertex vertex,
				final Point recyclePoint, final RenderModel renderModel) {
			vertexHeap.x = (float) vertex.x;
			vertexHeap.y = (float) vertex.y;
			vertexHeap.z = (float) vertex.z;
			vertexHeap.w = 1;
			skinBonesMatrixSumHeap.setZero();

			final List<GeosetVertexBoneLink> links = vertex.getLinks();
			if (links.size() > 0) {
				for (final GeosetVertexBoneLink link : links) {
					if (link.bone == null) {
						continue;
					}
					final Matrix4f worldMatrix = renderModel.getRenderNode(link.bone).getWorldMatrix();

					skinBonesMatrixSumHeap.m00 += (worldMatrix.m00 * link.weight) / 255f;
					skinBonesMatrixSumHeap.m01 += (worldMatrix.m01 * link.weight) / 255f;
					skinBonesMatrixSumHeap.m02 += (worldMatrix.m02 * link.weight) / 255f;
					skinBonesMatrixSumHeap.m03 += (worldMatrix.m03 * link.weight) / 255f;
					skinBonesMatrixSumHeap.m10 += (worldMatrix.m10 * link.weight) / 255f;
					skinBonesMatrixSumHeap.m11 += (worldMatrix.m11 * link.weight) / 255f;
					skinBonesMatrixSumHeap.m12 += (worldMatrix.m12 * link.weight) / 255f;
					skinBonesMatrixSumHeap.m13 += (worldMatrix.m13 * link.weight) / 255f;
					skinBonesMatrixSumHeap.m20 += (worldMatrix.m20 * link.weight) / 255f;
					skinBonesMatrixSumHeap.m21 += (worldMatrix.m21 * link.weight) / 255f;
					skinBonesMatrixSumHeap.m22 += (worldMatrix.m22 * link.weight) / 255f;
					skinBonesMatrixSumHeap.m23 += (worldMatrix.m23 * link.weight) / 255f;
					skinBonesMatrixSumHeap.m30 += (worldMatrix.m30 * link.weight) / 255f;
					skinBonesMatrixSumHeap.m31 += (worldMatrix.m31 * link.weight) / 255f;
					skinBonesMatrixSumHeap.m32 += (worldMatrix.m32 * link.weight) / 255f;
					skinBonesMatrixSumHeap.m33 += (worldMatrix.m33 * link.weight) / 255f;
				}
			}
			else {
				skinBonesMatrixSumHeap.setIdentity();
			}
			Matrix4f.transform(skinBonesMatrixSumHeap, vertexHeap, vertexSumHeap);

			recyclePoint.x = (int) coordinateSystem
					.convertX(Vertex.getCoord(vertexSumHeap, coordinateSystem.getPortFirstXYZ()));
			recyclePoint.y = (int) coordinateSystem
					.convertY(Vertex.getCoord(vertexSumHeap, coordinateSystem.getPortSecondXYZ()));
			return recyclePoint;
		}

		private Util() {
		}
	}
}
