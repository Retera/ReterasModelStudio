package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.ResettableIdObjectRenderer;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vector2;

public final class Graphics2DToTVertexModelElementRendererAdapter implements TVertexModelElementRenderer {
	private Graphics2D graphics;
	private CoordinateSystem coordinateSystem;
	private final Point recyclePointA = new Point(), recyclePointB = new Point(), recyclePointC = new Point();
	private final int[] recycleXCoords = new int[3];
	private final int[] recycleYCoords = new int[3];
	private final ProgramPreferences programPreferences;
	private final int vertexSize;
	private final ResettableIdObjectRenderer idObjectRenderer;

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
	public void renderFace(final Color borderColor, final Color color, final Vector2 a, final Vector2 b,
			final Vector2 c) {
		graphics.setColor(color);
		CoordinateSystem.Util.convertToPoint(coordinateSystem, a, recyclePointA);
		CoordinateSystem.Util.convertToPoint(coordinateSystem, b, recyclePointB);
		CoordinateSystem.Util.convertToPoint(coordinateSystem, c, recyclePointC);
		recycleXCoords[0] = recyclePointA.x;
		recycleXCoords[1] = recyclePointB.x;
		recycleXCoords[2] = recyclePointC.x;
		recycleYCoords[0] = recyclePointA.y;
		recycleYCoords[1] = recyclePointB.y;
		recycleYCoords[2] = recyclePointC.y;
		graphics.fillPolygon(recycleXCoords, recycleYCoords, 3);
		graphics.setColor(borderColor);
		graphics.drawPolygon(recycleXCoords, recycleYCoords, 3);
	}

	@Override
	public void renderVertex(final Color color, final Vector2 vertex) {
		CoordinateSystem.Util.convertToPoint(coordinateSystem, vertex, recyclePointA);
		graphics.setColor(color);
		graphics.fillRect(recyclePointA.x - (vertexSize / 2), (int) (recyclePointA.y - (vertexSize / 2.0)), vertexSize,
				vertexSize);
	}

}
