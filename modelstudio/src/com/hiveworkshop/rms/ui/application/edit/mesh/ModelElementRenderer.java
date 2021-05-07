package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.ResettableIdObjectRenderer;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public class ModelElementRenderer {
	private Graphics2D graphics;
	private CoordinateSystem coordinateSystem;
	private int vertexSize;
	private ResettableIdObjectRenderer idObjectRenderer;
	private RenderModel renderModel;
	private boolean isAnimated;

	public ModelElementRenderer(int vertexSize) {
		this.vertexSize = vertexSize;
		idObjectRenderer = new ResettableIdObjectRenderer(vertexSize);
	}

	public ModelElementRenderer reset(Graphics2D graphics, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated) {
		this.graphics = graphics;
		this.coordinateSystem = coordinateSystem;
		this.renderModel = renderModel;
		this.isAnimated = isAnimated;
		return this;
	}

	public void renderFace(Color borderColor, Color color, GeosetVertex a, GeosetVertex b, GeosetVertex c) {
		graphics.setColor(color);
		System.out.println("ugg static");

		Point vertexA = CoordSysUtils.convertToViewPoint(coordinateSystem, a);
		Point vertexB = CoordSysUtils.convertToViewPoint(coordinateSystem, b);
		Point vertexC = CoordSysUtils.convertToViewPoint(coordinateSystem, c);
//		Point vertexA = CoordSysUtils.convertToViewPoint(coordinateSystem, a, null);
//		Point vertexB = CoordSysUtils.convertToViewPoint(coordinateSystem, b, null);
//		Point vertexC = CoordSysUtils.convertToViewPoint(coordinateSystem, c, null);

		GU.fillPolygon(graphics, vertexA, vertexB, vertexC);
		graphics.setColor(borderColor);
		GU.drawPolygon(graphics, vertexA, vertexB, vertexC);
	}

	public void renderVertex(Color color, Vec3 vertex) {
		Point point = CoordSysUtils.convertToViewPoint(coordinateSystem, vertex);
		graphics.setColor(color);
		GU.fillCenteredSquare(graphics, point, vertexSize);
	}

	public void renderIdObject(IdObject object) {
		ResettableIdObjectRenderer idObjectRenderer = this.idObjectRenderer.reset(coordinateSystem, graphics, renderModel, isAnimated, true);

		idObjectRenderer.renderIdObject(object);
	}

	public void renderCamera(Camera camera, Color boxColor, Vec3 position, Color targetColor, Vec3 targetPosition) {
		if (isAnimated) {
			throw new WrongModeException("not animating cameras yet, code not finished");
		}
		Graphics2D g2 = ((Graphics2D) graphics.create());

		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();
		Point start = new Point(
				(int) Math.round(coordinateSystem.viewX(position.getCoord(dim1))),
				(int) Math.round(coordinateSystem.viewY(position.getCoord(dim2))));
		Point end = new Point(
				(int) Math.round(coordinateSystem.viewX(targetPosition.getCoord(dim1))),
				(int) Math.round(coordinateSystem.viewY(targetPosition.getCoord(dim2))));

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
}
