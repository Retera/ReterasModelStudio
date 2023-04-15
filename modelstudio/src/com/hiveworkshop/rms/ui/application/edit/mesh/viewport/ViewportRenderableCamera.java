package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
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

	Vec3 f = new Vec3();
	Vec3 s = new Vec3();
	Vec3 u = new Vec3();
	private void lookAt(Vec3 eye, Vec3 center, Vec3 up) {
		f.set(center).sub(eye).normalize();
		s.set(f).cross(up).normalize();
		u.set(s).cross(f);

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

	Mat4 scaleTranslateMatrix = new Mat4();
	Vec3 vector3heap = new Vec3();
	public void render(Graphics2D graphics, CoordinateSystem coordinateSystem,
	                   Vec3 start, Vec3 end, double rotation) {
		float length = Vec3.getDiff(end, start).length();
		double cameraModelScale = length / cameraLength;

		lookAt(start, end, Vec3.Z_AXIS);

		scaleTranslateMatrix.setIdentity().translate(end).mul(rotationMatrix);
		vector3heap.set(1, 1, 1).scale((float) cameraModelScale);
		scaleTranslateMatrix.scale(vector3heap);

		Point[] points = new Point[3];
		for (int i = 0; i < points.length; i++) {
			points[i] = new Point(0, 0);
		}

		for (Geoset geoset : cameraModel.getGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				for (int i = 0; i < 3; i++) {
					GeosetVertex vertex = triangle.getVerts()[i];
					vector3heap.set(vertex).transform(scaleTranslateMatrix, 1, true);
					Vec2 vec2 = coordinateSystem.viewV(vector3heap);
					points[i].x = (int) vec2.x;
					points[i].y = (int) vec2.y;
				}
				graphics.drawLine(points[0].x, points[0].y, points[1].x, points[1].y);
				graphics.drawLine(points[2].x, points[2].y, points[1].x, points[1].y);
				graphics.drawLine(points[2].x, points[2].y, points[0].x, points[0].y);
			}
		}
	}




	Vec3 vertexHeap1 = new Vec3();
	Vec3 vertexHeap2 = new Vec3();
	public void camera(Camera camera, Graphics2D graphics, boolean isAnimated, RenderModel renderModel, CoordinateSystem coordinateSystem) {
		graphics.setColor(Color.GREEN.darker());
		Graphics2D g2 = ((Graphics2D) graphics.create());

		vertexHeap1.set(getTransformedCoord2(isAnimated, renderModel, camera.getSourceNode(), camera.getPosition()));
		vertexHeap1.set(getTransformedCoord2(isAnimated, renderModel, camera.getTargetNode(), camera.getTargetPosition()));

		float renderRotationScalar = 0;
		if (renderModel != null && renderModel.getTimeEnvironment() != null) {
			renderRotationScalar = camera.getSourceNode().getRenderRotationScalar(renderModel.getTimeEnvironment());
		}

		render(g2, coordinateSystem, vertexHeap1, vertexHeap2, renderRotationScalar);
	}

	public void renderCamera(Camera camera, Graphics2D graphics, RenderModel renderModel, boolean isAnimated, CoordinateSystem coordinateSystem) {
		graphics.setColor(Color.GREEN.darker());
		Graphics2D g2 = ((Graphics2D) graphics.create());

		vertexHeap1.set(getTransformedCoord2(isAnimated, renderModel, camera.getSourceNode(), camera.getPosition()));
		vertexHeap1.set(getTransformedCoord2(isAnimated, renderModel, camera.getTargetNode(), camera.getTargetPosition()));

		float renderRotationScalar = 0;
		if (renderModel != null && renderModel.getTimeEnvironment() != null) {
			renderRotationScalar = camera.getSourceNode().getRenderRotationScalar(renderModel.getTimeEnvironment());
		}

		render(g2, coordinateSystem, vertexHeap1, vertexHeap2, renderRotationScalar);
	}

	public void renderCamera(Graphics2D graphics, CoordinateSystem coordinateSystem, boolean isAnimated, Camera camera,
	                         int vertexSize, Color boxColor, Vec3 position, Color targetColor, Vec3 targetPosition) {
		if (isAnimated) {
			throw new WrongModeException("not animating cameras yet, code not finished");
		}
		Graphics2D g2 = ((Graphics2D) graphics.create());

		Vec2 posV = coordinateSystem.viewV(position);
		Point start = new Point(Math.round(posV.x), Math.round(posV.y));

		Vec2 targV = coordinateSystem.viewV(position);
		Point end = new Point(Math.round(targV.x), Math.round(targV.y));

		g2.translate(end.x, end.y);
		g2.rotate(-(Math.PI / 2 + Math.atan2(end.x - start.x, end.y - start.y)));
		double zoom = coordinateSystem.getZoom();
		int size = (int) (20 * zoom);
		double dist = start.distance(end);

		g2.setColor(boxColor);
		g2.fillRect((int) dist - vertexSize, 0 - vertexSize, 1 + vertexSize * 2, 1 + vertexSize * 2);
		g2.drawRect((int) dist - size, -size, size * 2, size * 2);

		g2.setColor(targetColor);

		g2.fillRect(0 - vertexSize, 0 - vertexSize, 1 + vertexSize * 2, 1 + vertexSize * 2);
		g2.drawLine(0, 0, size, size);
		g2.drawLine(0, 0, size, -size);

		g2.drawLine(0, 0, (int) dist, 0);
	}

	public Vec3 getTransformedCoord2(boolean isAnimated, RenderModel renderModel, AnimatedNode object, Vec3 point) {
		vector3heap.set(point);
		if(isAnimated && renderModel != null){
			RenderNode<AnimatedNode> renderNode = renderModel.getRenderNode(object);
			if(renderNode != null){
				vector3heap.transform(renderNode.getWorldMatrix());

			}
		}
		return vector3heap;
	}
}
