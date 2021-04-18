package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.ResettableIdObjectRenderer;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.GU;
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
		Point pointA = CoordinateSystem.Util.convertToViewPoint(coordinateSystem, a);
		Point pointB = CoordinateSystem.Util.convertToViewPoint(coordinateSystem, b);
		Point pointC = CoordinateSystem.Util.convertToViewPoint(coordinateSystem, c);

		graphics.setColor(color);
		GU.fillPolygon(graphics, pointA, pointB, pointC);
		graphics.setColor(borderColor);
		GU.drawPolygon(graphics, pointA, pointB, pointC);
	}

	@Override
	public void renderVertex(final Color color, final Vec2 vertex) {
		Point pointA = CoordinateSystem.Util.convertToViewPoint(coordinateSystem, vertex);
		graphics.setColor(color);
		GU.fillCenteredSquare(graphics, pointA, vertexSize);
//		graphics.fillRect(pointA.x - (vertexSize / 2), (int) (pointA.y - (vertexSize / 2.0)), vertexSize, vertexSize);
	}

}
