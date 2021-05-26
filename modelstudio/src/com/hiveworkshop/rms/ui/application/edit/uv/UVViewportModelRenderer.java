package com.hiveworkshop.rms.ui.application.edit.uv;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;

public class UVViewportModelRenderer {
	private int vertexSize;

	public UVViewportModelRenderer() {
		vertexSize = ProgramGlobals.getPrefs().getVertexSize();
	}

	public void drawGeosetUVs(Graphics2D graphics,
	                          CoordinateSystem coordinateSystem,
	                          ModelHandler modelHandler) {

		for (Geoset geoset : modelHandler.getModel().getGeosets()) {
			graphics.setColor(ProgramGlobals.getPrefs().getTriangleColor());
			if (modelHandler.getModelView().getHighlightedGeoset() == geoset) {
				graphics.setColor(ProgramGlobals.getPrefs().getHighlighTriangleColor());
			}
			for (Triangle triangle : geoset.getTriangles()) {
				Vec2[] triV2 = new Vec2[3];
				int vIndex = 0;
				for (GeosetVertex vertex : triangle.getVerts()) {

					triV2[vIndex] = CoordSysUtils.convertToViewVec2(coordinateSystem, vertex.getTVertex(0));

					vIndex++;
				}
				GU.drawPolygon(graphics, triV2);
			}
		}
	}

	public void renderFace(Graphics2D graphics,
	                       CoordinateSystem coordinateSystem,
	                       Color borderColor,
	                       Color color,
	                       Vec2 a, Vec2 b, Vec2 c) {
		Vec2 pointA = CoordSysUtils.convertToViewVec2(coordinateSystem, a);
		Vec2 pointB = CoordSysUtils.convertToViewVec2(coordinateSystem, b);
		Vec2 pointC = CoordSysUtils.convertToViewVec2(coordinateSystem, c);

		graphics.setColor(color);
		GU.fillPolygon(graphics, pointA, pointB, pointC);
		graphics.setColor(borderColor);
		GU.drawPolygon(graphics, pointA, pointB, pointC);
	}

	public void renderVertex(Graphics2D graphics,
	                         CoordinateSystem coordinateSystem,
	                         Color color, Vec2 vertex) {
		Vec2 pointA = CoordSysUtils.convertToViewVec2(coordinateSystem, vertex);
		graphics.setColor(color);
		GU.fillCenteredSquare(graphics, pointA, vertexSize);
//		graphics.fillRect(pointA.x - (vertexSize / 2), (int) (pointA.y - (vertexSize / 2.0)), vertexSize, vertexSize);
	}
}
