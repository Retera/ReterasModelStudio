package com.hiveworkshop.wc3.gui.modeledit.viewport;

import java.awt.Graphics2D;
import java.awt.Point;
import java.io.IOException;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

import de.wc3data.stream.BlizzardDataInputStream;

public class ViewportRenderableCamera {
	private final MDL cameraModel;
	private double cameraLength;
	private final Matrix4f rotationMatrix = new Matrix4f();
	private final Matrix4f scaleTranslateMatrix = new Matrix4f();
	private final Matrix4f translateMatrix = new Matrix4f();
	private static Vector3f f = new Vector3f(), u = new Vector3f(), s = new Vector3f();
	private static final Vertex start = new Vertex(0, 0, 0);
	private static final Vertex end = new Vertex(0, 0, 0);
	private static final Vector3f startVector = new Vector3f(0, 0, 0);
	private static final Vector3f endVector = new Vector3f(0, 0, 0);
	private static final Vector3f delta = new Vector3f(0, 0, 0);
	private static final Vector3f vector3heap = new Vector3f(0, 0, 0);
	private static final Vector3f Z_DIMENSION = new Vector3f(0, 0, 1);
	private static final Quaternion quatHeap = new Quaternion();
	private static final QuaternionRotation quatRotHeap = new QuaternionRotation(0, 0, 0, 0);
	private static final Vector4f vectorHeap = new Vector4f();
	private static final Vector3f ZEROES = new Vector3f(0, 0, 0);
	private static final Vector3f ONES = new Vector3f(1, 1, 1);
	private static final Vertex quatRotAxisHeap = new Vertex(0, 0, 0);

	public ViewportRenderableCamera() {
		MDL camera;
		try {
			camera = new MDL(MdxUtils.loadModel(new BlizzardDataInputStream(
					MpqCodebase.get().getResourceAsStream("Objects\\CameraHelper\\CameraHelper.mdx"))));
			cameraLength = Math.abs(camera.getIdObject(0).getPivotPoint().x);
		} catch (final IOException e) {
			camera = null;
			e.printStackTrace();
			ExceptionPopup.display(e);
		}
		this.cameraModel = camera;
	}

	private void lookAt(final Vector3f eye, final Vector3f center, final Vector3f up) {
		Vector3f.sub(center, eye, f);
		f.normalise(f);
		u.set(up);
		u.normalise();
		Vector3f.cross(f, u, s);
		s.normalise();
		Vector3f.cross(s, f, u);

		rotationMatrix.setIdentity();
		rotationMatrix.m00 = f.x;
		rotationMatrix.m01 = f.y;
		rotationMatrix.m02 = f.z;
		rotationMatrix.m10 = s.x;
		rotationMatrix.m11 = s.y;
		rotationMatrix.m12 = s.z;
		rotationMatrix.m20 = u.x;
		rotationMatrix.m21 = u.y;
		rotationMatrix.m22 = u.z;
	}

	public void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem, final double startX,
			final double startY, final float startZ, final double endX, final double endY, final float endZ,
			final double rotation) {
		start.setTo(Vertex.ORIGIN);
		end.setTo(Vertex.ORIGIN);
		start.x = startX;
		start.y = startY;
		start.z = startZ;
		end.x = endX;
		end.y = endY;
		end.z = endZ;

		startVector.x = (float) start.x;
		startVector.y = (float) start.y;
		startVector.z = (float) start.z;
		endVector.x = (float) end.x;
		endVector.y = (float) end.y;
		endVector.z = (float) end.z;

		delta.set((float) end.x, (float) end.y, (float) end.z);
		delta.x -= (float) start.x;
		delta.y -= (float) start.y;
		delta.z -= (float) start.z;
		final float length = delta.length();
		final double cameraModelScale = length / cameraLength;

		lookAt(startVector, endVector, Z_DIMENSION);
		scaleTranslateMatrix.setIdentity();
		vector3heap.set((float) (end.x), (float) (end.y), (float) (end.z));
		scaleTranslateMatrix.translate(vector3heap);
		Matrix4f.mul(scaleTranslateMatrix, rotationMatrix, scaleTranslateMatrix);
		vector3heap.set((float) cameraModelScale, (float) cameraModelScale, (float) cameraModelScale);
		scaleTranslateMatrix.scale(vector3heap);

		final Point[] points = new Point[3];
		for (int i = 0; i < points.length; i++) {
			points[i] = new Point(0, 0);
		}
		for (final Geoset geoset : cameraModel.getGeosets()) {
			for (final Triangle triangle : geoset.getTriangles()) {
				for (int i = 0; i < 3; i++) {
					final GeosetVertex vertex = triangle.getVerts()[i];
					vectorHeap.x = (float) (vertex.x);
					vectorHeap.y = (float) (vertex.y);
					vectorHeap.z = (float) (vertex.z);
					vectorHeap.w = 1;
					Matrix4f.transform(scaleTranslateMatrix, vectorHeap, vectorHeap);
					points[i].x = (int) coordinateSystem
							.convertX(Vertex.getCoord(vectorHeap, coordinateSystem.getPortFirstXYZ()));
					points[i].y = (int) coordinateSystem
							.convertY(Vertex.getCoord(vectorHeap, coordinateSystem.getPortSecondXYZ()));
				}
				graphics.drawLine(points[0].x, points[0].y, points[1].x, points[1].y);
				graphics.drawLine(points[2].x, points[2].y, points[1].x, points[1].y);
				graphics.drawLine(points[2].x, points[2].y, points[0].x, points[0].y);
			}
		}
	}

	public void render(final double startX, final double startY, final double startZ, final double endX,
			final double endY, final double endZ, final double rotation) {
		start.x = startX;
		start.y = startY;
		start.z = startZ;
		end.x = endX;
		end.y = endY;
		end.z = endZ;

		startVector.x = (float) start.x;
		startVector.y = (float) start.y;
		startVector.z = (float) start.z;
		endVector.x = (float) end.x;
		endVector.y = (float) end.y;
		endVector.z = (float) end.z;

		delta.set((float) end.x, (float) end.y, (float) end.z);
		delta.x -= (float) start.x;
		delta.y -= (float) start.y;
		delta.z -= (float) start.z;
		final float length = delta.length();
		final double cameraModelScale = length / cameraLength;

		lookAt(startVector, endVector, Z_DIMENSION);
		scaleTranslateMatrix.setIdentity();
		vector3heap.set((float) (end.x), (float) (end.y), (float) (end.z));
		scaleTranslateMatrix.translate(vector3heap);
		Matrix4f.mul(rotationMatrix, scaleTranslateMatrix, scaleTranslateMatrix);
		vector3heap.set((float) cameraModelScale, (float) cameraModelScale, (float) cameraModelScale);
		scaleTranslateMatrix.scale(vector3heap);

		final Point[] points = new Point[3];
		for (int i = 0; i < points.length; i++) {
			points[i] = new Point(0, 0);
		}
		for (final Geoset geoset : cameraModel.getGeosets()) {
			for (final Triangle triangle : geoset.getTriangles()) {
				for (int i = 0; i < 3; i++) {
					final GeosetVertex vertex = triangle.getVerts()[i];
					vectorHeap.x = (float) (vertex.x);
					vectorHeap.y = (float) (vertex.y);
					vectorHeap.z = (float) (vertex.z);
					vectorHeap.w = 1;
					Matrix4f.transform(scaleTranslateMatrix, vectorHeap, vectorHeap);
				}
			}
		}
	}
}
