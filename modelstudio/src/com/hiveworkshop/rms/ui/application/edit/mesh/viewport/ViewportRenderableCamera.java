package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.awt.*;
import java.io.IOException;

public class ViewportRenderableCamera {
	private final EditableModel cameraModel;
	private double cameraLength;
	private final Mat4 rotationMatrix = new Mat4();
	private final Mat4 scaleTranslateMatrix = new Mat4();
	private final Mat4 translateMatrix = new Mat4();
	private static final Vec3 start = new Vec3(0, 0, 0);
	private static final Vec3 end = new Vec3(0, 0, 0);
	private static final Vec3 startVector = new Vec3(0, 0, 0);
	private static final Vec3 endVector = new Vec3(0, 0, 0);
	private static final Vec3 vector3heap = new Vec3(0, 0, 0);
	private static final Vec3 Z_DIMENSION = new Vec3(0, 0, 1);

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

	private void lookAt(final Vec3 eye, final Vec3 center, final Vec3 up) {
		Vec3 f = Vec3.getDiff(center, eye);
		f.normalize();
		Vec3 s = Vec3.getCross(f, Vec3.getNormalized(up));
		s.normalize();
		Vec3 u = Vec3.getCross(s, f);

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

	public void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
	                   final double startX, final double startY, final double startZ,
	                   final double endX, final double endY, final float endZ,
	                   final double rotation) {
		start.set(startX, startY, startZ);
		end.set(endX, endY, endZ);

		startVector.set(start);
		endVector.set(end);

		Vec3 delta = Vec3.getDiff(end, start);

		final float length = delta.length();
		final double cameraModelScale = length / cameraLength;

		lookAt(startVector, endVector, Z_DIMENSION);
		scaleTranslateMatrix.setIdentity();
		vector3heap.set(end);
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
					Vec4 vectorHeap = new Vec4(vertex, 1);
					vectorHeap.transform(scaleTranslateMatrix);
					points[i].x = (int) coordinateSystem.convertX(vectorHeap.getCoord(coordinateSystem.getPortFirstXYZ()));
					points[i].y = (int) coordinateSystem.convertY(vectorHeap.getCoord(coordinateSystem.getPortSecondXYZ()));
				}
				graphics.drawLine(points[0].x, points[0].y, points[1].x, points[1].y);
				graphics.drawLine(points[2].x, points[2].y, points[1].x, points[1].y);
				graphics.drawLine(points[2].x, points[2].y, points[0].x, points[0].y);
			}
		}
	}

	public void render(final double startX, final double startY, final double startZ,
	                   final double endX, final double endY, final double endZ,
	                   final double rotation) {
		start.set(startX, startY, startZ);
		end.set(endX, endY, endZ);

		startVector.set(start);
		endVector.set(end);

		Vec3 delta = Vec3.getDiff(end, start);

		final float length = delta.length();
		final double cameraModelScale = length / cameraLength;

		lookAt(startVector, endVector, Z_DIMENSION);
		scaleTranslateMatrix.setIdentity();
		vector3heap.set(end);
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
					Vec4 vectorHeap = new Vec4(vertex, 1);
					vectorHeap.transform(scaleTranslateMatrix);
				}
			}
		}
	}
}
