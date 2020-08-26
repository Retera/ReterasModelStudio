package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import java.awt.Graphics2D;
import java.awt.Point;
import java.io.IOException;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vector3;
import com.hiveworkshop.rms.util.Vector4;

public class ViewportRenderableCamera {
	private final EditableModel cameraModel;
	private double cameraLength;
	private final Mat4 rotationMatrix = new Mat4();
	private final Mat4 scaleTranslateMatrix = new Mat4();
	private final Mat4 translateMatrix = new Mat4();
	private static final Vector3 f = new Vector3();
	private static final Vector3 u = new Vector3();
	private static final Vector3 s = new Vector3();
	private static final Vector3 start = new Vector3(0, 0, 0);
	private static final Vector3 end = new Vector3(0, 0, 0);
	private static final Vector3 startVector = new Vector3(0, 0, 0);
	private static final Vector3 endVector = new Vector3(0, 0, 0);
	private static final Vector3 delta = new Vector3(0, 0, 0);
	private static final Vector3 vector3heap = new Vector3(0, 0, 0);
	private static final Vector3 Z_DIMENSION = new Vector3(0, 0, 1);
	private static final Quat quatHeap = new Quat();
	private static final Quat quatRotHeap = new Quat(0, 0, 0, 0);
	private static final Vector4 vectorHeap = new Vector4();
	private static final Vector3 ZEROES = new Vector3(0, 0, 0);
	private static final Vector3 ONES = new Vector3(1, 1, 1);
	private static final Vector3 quatRotAxisHeap = new Vector3(0, 0, 0);

	public ViewportRenderableCamera() {
		EditableModel camera;
		try {
			camera = new EditableModel(MdxUtils.loadMdlx(GameDataFileSystem.getDefault().getResourceAsStream("Objects\\CameraHelper\\CameraHelper.mdx")));
			cameraLength = Math.abs(camera.getIdObject(0).getPivotPoint().x);
		} catch (final IOException e) {
			camera = null;
			e.printStackTrace();
			ExceptionPopup.display(e);
		}
		cameraModel = camera;
	}

	private void lookAt(final Vector3 eye, final Vector3 center, final Vector3 up) {
		center.sub(eye, f);
		f.normalize();
		u.set(up);
		u.normalize();
		f.cross(u, s);
		s.normalize();
		s.cross(f, u);

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
			final double startY, final double startZ, final double endX, final double endY, final float endZ,
			final double rotation) {
		start.set(startX, startY, startZ);
		end.set(endX, endY, endZ);

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
		scaleTranslateMatrix.mul(rotationMatrix);
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
					scaleTranslateMatrix.transform(vectorHeap);
					points[i].x = (int) coordinateSystem
							.convertX(Vector3.getCoord(vectorHeap, coordinateSystem.getPortFirstXYZ()));
					points[i].y = (int) coordinateSystem
							.convertY(Vector3.getCoord(vectorHeap, coordinateSystem.getPortSecondXYZ()));
				}
				graphics.drawLine(points[0].x, points[0].y, points[1].x, points[1].y);
				graphics.drawLine(points[2].x, points[2].y, points[1].x, points[1].y);
				graphics.drawLine(points[2].x, points[2].y, points[0].x, points[0].y);
			}
		}
	}

	public void render(final double startX, final double startY, final double startZ, final double endX,
			final double endY, final double endZ, final double rotation) {
		start.set(startX, startY, startZ);
		end.set(endX, endY, endZ);

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
		rotationMatrix.mul(scaleTranslateMatrix, scaleTranslateMatrix);
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
					scaleTranslateMatrix.transform(vectorHeap);
				}
			}
		}
	}
}
