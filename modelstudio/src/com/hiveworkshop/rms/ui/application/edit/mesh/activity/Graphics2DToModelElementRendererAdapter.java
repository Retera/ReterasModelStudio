package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.NodeIconPalette;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.ResettableIdObjectRenderer;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public final class Graphics2DToModelElementRendererAdapter implements ModelElementRenderer {
	private Graphics2D graphics;
	private CoordinateSystem coordinateSystem;
	private ProgramPreferences programPreferences;
	private int vertexSize;
	private ResettableIdObjectRenderer idObjectRenderer;

	public Graphics2DToModelElementRendererAdapter(int vertexSize, ProgramPreferences programPreferences) {
		this.vertexSize = vertexSize;
		this.programPreferences = programPreferences;
		idObjectRenderer = new ResettableIdObjectRenderer(vertexSize);
	}

	public Graphics2DToModelElementRendererAdapter reset(Graphics2D graphics, CoordinateSystem coordinateSystem) {
		this.graphics = graphics;
		this.coordinateSystem = coordinateSystem;
		return this;
	}

	@Override
	public void renderFace(Color borderColor, Color color, GeosetVertex a, GeosetVertex b, GeosetVertex c) {
		graphics.setColor(color);


		Point vertexA = CoordinateSystem.Util.convertToViewPoint(coordinateSystem, a);
		Point vertexB = CoordinateSystem.Util.convertToViewPoint(coordinateSystem, b);
		Point vertexC = CoordinateSystem.Util.convertToViewPoint(coordinateSystem, c);

		int[] polygonX = new int[3];
		polygonX[0] = vertexA.x;
		polygonX[1] = vertexB.x;
		polygonX[2] = vertexC.x;

		int[] polygonY = new int[3];
		polygonY[0] = vertexA.y;
		polygonY[1] = vertexB.y;
		polygonY[2] = vertexC.y;

		graphics.fillPolygon(polygonX, polygonY, 3);
		graphics.setColor(borderColor);
		graphics.drawPolygon(polygonX, polygonY, 3);
//		GU.fillPolygon(graphics, vertexA, vertexB, vertexC);
//		GU.drawPolygon(graphics, vertexA, vertexB, vertexC);
	}

	@Override
	public void renderVertex(Color color, Vec3 vertex) {
		Point point = CoordinateSystem.Util.convertToViewPoint(coordinateSystem, vertex);
		graphics.setColor(color);
		graphics.fillRect(point.x - vertexSize / 2, (int) (point.y - (vertexSize / 2.0)), vertexSize, vertexSize);
	}

	@Override
	public void renderIdObject(IdObject object, NodeIconPalette nodeIconPalette, Color lightColor, Color pivotPointColor) {
		object.apply(idObjectRenderer.reset(coordinateSystem, graphics, lightColor, pivotPointColor, nodeIconPalette, programPreferences.isUseBoxesForPivotPoints()));
	}

	@Override
	public void renderCamera(Camera camera, Color boxColor, Vec3 position, Color targetColor, Vec3 targetPosition) {
		Graphics2D g2 = ((Graphics2D) graphics.create());
		// boolean verSel = selection.contains(ver);
		// boolean tarSel = selection.contains(targ);
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
		double zoom = CoordinateSystem.Util.getZoom(coordinateSystem);
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
