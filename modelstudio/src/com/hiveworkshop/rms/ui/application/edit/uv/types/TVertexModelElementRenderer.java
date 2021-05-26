package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;

public class TVertexModelElementRenderer {
	private Graphics2D graphics;
	private CoordinateSystem coordinateSystem;
	private int vertexSize;

	public TVertexModelElementRenderer(int vertexSize) {
		this.vertexSize = vertexSize;
	}

	public TVertexModelElementRenderer reset(Graphics2D graphics, CoordinateSystem coordinateSystem) {
		this.graphics = graphics;
		this.coordinateSystem = coordinateSystem;
		return this;
	}

	public void renderFace(Color borderColor, Color color, Vec2 a, Vec2 b, Vec2 c) {
		Vec2  pointA = CoordSysUtils.convertToViewVec2(coordinateSystem, a);
		Vec2  pointB = CoordSysUtils.convertToViewVec2(coordinateSystem, b);
		Vec2  pointC = CoordSysUtils.convertToViewVec2(coordinateSystem, c);

		graphics.setColor(color);
		GU.fillPolygon(graphics, pointA, pointB, pointC);
		graphics.setColor(borderColor);
		GU.drawPolygon(graphics, pointA, pointB, pointC);
	}

	public void renderVertex(Color color, Vec2 vertex) {
		Vec2 pointA = CoordSysUtils.convertToViewVec2(coordinateSystem, vertex);
		graphics.setColor(color);
		GU.fillCenteredSquare(graphics, pointA, vertexSize);
//		graphics.fillRect(pointA.x - (vertexSize / 2), (int) (pointA.y - (vertexSize / 2.0)), vertexSize, vertexSize);
	}
}
