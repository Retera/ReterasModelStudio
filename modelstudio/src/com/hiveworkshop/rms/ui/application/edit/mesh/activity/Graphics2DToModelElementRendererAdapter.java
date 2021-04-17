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

	public Graphics2DToModelElementRendererAdapter(final int vertexSize, final ProgramPreferences programPreferences) {
		this.vertexSize = vertexSize;
		this.programPreferences = programPreferences;
		idObjectRenderer = new ResettableIdObjectRenderer(vertexSize);
	}

	public Graphics2DToModelElementRendererAdapter reset(final Graphics2D graphics, final CoordinateSystem coordinateSystem) {
		this.graphics = graphics;
		this.coordinateSystem = coordinateSystem;
		return this;
	}

	@Override
	public void renderFace(final Color borderColor, final Color color, final GeosetVertex a, final GeosetVertex b, final GeosetVertex c) {
		graphics.setColor(color);


		Point vertexA = CoordinateSystem.Util.convertToPoint(coordinateSystem, a);
		Point vertexB = CoordinateSystem.Util.convertToPoint(coordinateSystem, b);
		Point vertexC = CoordinateSystem.Util.convertToPoint(coordinateSystem, c);

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
	public void renderVertex(final Color color, final Vec3 vertex) {
		Point point = CoordinateSystem.Util.convertToPoint(coordinateSystem, vertex);
		graphics.setColor(color);
		graphics.fillRect(point.x - vertexSize / 2, (int) (point.y - (vertexSize / 2.0)), vertexSize, vertexSize);
	}

	@Override
	public void renderIdObject(final IdObject object, final NodeIconPalette nodeIconPalette, final Color lightColor, final Color pivotPointColor) {
		object.apply(idObjectRenderer.reset(coordinateSystem, graphics, lightColor, pivotPointColor, nodeIconPalette, programPreferences.isUseBoxesForPivotPoints()));
	}

	@Override
	public void renderCamera(final Camera camera, final Color boxColor, final Vec3 position, final Color targetColor,
			final Vec3 targetPosition) {
		final Graphics2D g2 = ((Graphics2D) graphics.create());
		// final boolean verSel = selection.contains(ver);
		// final boolean tarSel = selection.contains(targ);
		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();
		final Point start = new Point(
				(int) Math.round(coordinateSystem.convertX(position.getCoord(dim1))),
				(int) Math.round(coordinateSystem.convertY(position.getCoord(dim2))));
		final Point end = new Point(
				(int) Math.round(coordinateSystem.convertX(targetPosition.getCoord(dim1))),
				(int) Math.round(coordinateSystem.convertY(targetPosition.getCoord(dim2))));

		g2.translate(end.x, end.y);
		g2.rotate(-(Math.PI / 2 + Math.atan2(end.x - start.x, end.y - start.y)));
		final double zoom = CoordinateSystem.Util.getZoom(coordinateSystem);
		final int size = (int) (20 * zoom);
		final double dist = start.distance(end);

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
