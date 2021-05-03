package com.hiveworkshop.rms.ui.application.edit.uv;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;

public class UVViewportModelRenderer {

	public UVViewportModelRenderer() {
	}

	public void drawGeosetUVs(Graphics2D graphics,
	                          ProgramPreferences programPreferences,
	                          CoordinateSystem coordinateSystem,
	                          ModelHandler modelHandler) {

		for (Geoset geoset : modelHandler.getModel().getGeosets()) {
			graphics.setColor(programPreferences.getTriangleColor());
			if (modelHandler.getModelView().getHighlightedGeoset() == geoset) {
				graphics.setColor(programPreferences.getHighlighTriangleColor());
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
}
