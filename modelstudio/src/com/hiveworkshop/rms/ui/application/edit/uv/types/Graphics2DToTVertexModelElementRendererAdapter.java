package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.ResettableIdObjectRenderer;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;

public final class Graphics2DToTVertexModelElementRendererAdapter implements TVertexModelElementRenderer {
	private Graphics2D graphics;
	private CoordinateSystem coordinateSystem;
	private ProgramPreferences programPreferences;
	private int vertexSize;
	private ResettableIdObjectRenderer idObjectRenderer;

	public Graphics2DToTVertexModelElementRendererAdapter(final int vertexSize,
	                                                      final ProgramPreferences programPreferences) {
		this.vertexSize = vertexSize;
		this.programPreferences = programPreferences;
		idObjectRenderer = new ResettableIdObjectRenderer(vertexSize);
	}

	public Graphics2DToTVertexModelElementRendererAdapter reset(final Graphics2D graphics,
	                                                            final CoordinateSystem coordinateSystem) {
		this.graphics = graphics;
		this.coordinateSystem = coordinateSystem;
		return this;
	}

	@Override
	public void renderFace(final Color borderColor, final Color color, final Vec2 a, final Vec2 b, final Vec2 c) {
		Point pointA = CoordinateSystem.Util.convertToPoint(coordinateSystem, a);
		Point pointB = CoordinateSystem.Util.convertToPoint(coordinateSystem, b);
		Point pointC = CoordinateSystem.Util.convertToPoint(coordinateSystem, c);

		int[] recycleXCoords = new int[3];
		int[] recycleYCoords = new int[3];

		recycleXCoords[0] = pointA.x;
		recycleXCoords[1] = pointB.x;
		recycleXCoords[2] = pointC.x;
		recycleYCoords[0] = pointA.y;
		recycleYCoords[1] = pointB.y;
		recycleYCoords[2] = pointC.y;

		graphics.setColor(color);
		graphics.fillPolygon(recycleXCoords, recycleYCoords, 3);
		graphics.setColor(borderColor);
		graphics.drawPolygon(recycleXCoords, recycleYCoords, 3);
	}

	@Override
	public void renderVertex(final Color color, final Vec2 vertex) {
		Point pointA = CoordinateSystem.Util.convertToPoint(coordinateSystem, vertex);
		graphics.setColor(color);
		graphics.fillRect(pointA.x - (vertexSize / 2), (int) (pointA.y - (vertexSize / 2.0)), vertexSize,
				vertexSize);
	}

}
