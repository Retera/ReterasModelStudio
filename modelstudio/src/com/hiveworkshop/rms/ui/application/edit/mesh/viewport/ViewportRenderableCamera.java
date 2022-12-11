package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.io.IOException;

public class ViewportRenderableCamera {
	private final EditableModel cameraModel;
	private double cameraLength;
	private final Mat4 rotationMatrix = new Mat4();
	private static final Vec3 Z_DIMENSION = new Vec3(0, 0, 1);

	public ViewportRenderableCamera() {
		EditableModel camera;
		try {
			String filepath = "Objects\\CameraHelper\\CameraHelper.mdx";
			camera = MdxUtils.loadEditable(filepath, null);
			cameraLength = Math.abs(camera.getIdObject(0).getPivotPoint().x);
		} catch (final IOException e) {
			camera = null;
			e.printStackTrace();
			ExceptionPopup.display(e);
		}
		cameraModel = camera;
	}

	private void lookAt(Vec3 eye, Vec3 center, Vec3 up) {
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

	public void render(Graphics2D graphics, CoordinateSystem coordinateSystem,
	                   Vec3 start, Vec3 end, double rotation) {
		float length = Vec3.getDiff(end, start).length();
		double cameraModelScale = length / cameraLength;

		lookAt(start, end, Z_DIMENSION);

		Mat4 scaleTranslateMatrix = new Mat4().setIdentity();
		scaleTranslateMatrix.translate(end);
		scaleTranslateMatrix.mul(rotationMatrix);
		Vec3 vector3heap = new Vec3(1, 1, 1).scale((float) cameraModelScale);
		scaleTranslateMatrix.scale(vector3heap);

		Point[] points = new Point[3];
		for (int i = 0; i < points.length; i++) {
			points[i] = new Point(0, 0);
		}

		Vec3 tempV3 = new Vec3();
		for (Geoset geoset : cameraModel.getGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				for (int i = 0; i < 3; i++) {
					GeosetVertex vertex = triangle.getVerts()[i];
					tempV3.set(vertex).transform(scaleTranslateMatrix, 1, true);
					Vec2 vec2 = coordinateSystem.viewV(tempV3);
					points[i].x = (int) vec2.x;
					points[i].y = (int) vec2.y;
				}
				graphics.drawLine(points[0].x, points[0].y, points[1].x, points[1].y);
				graphics.drawLine(points[2].x, points[2].y, points[1].x, points[1].y);
				graphics.drawLine(points[2].x, points[2].y, points[0].x, points[0].y);
			}
		}
	}
}
