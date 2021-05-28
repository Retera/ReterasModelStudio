package com.hiveworkshop.rms.ui.application.edit.uv;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;

public class UVViewportModelRenderer {
	private static final Color FACE_SELECTED_COLOR = new Color(1f, 0.45f, 0.45f, 0.3f);
	private static final Color FACE_HIGHLIGHT_COLOR = new Color(0.45f, 1f, 0.45f, 0.3f);
	private static final Color FACE_NOT_SELECTED_COLOR = new Color(0.45f, 0.45f, 1f, 0.3f);
	ProgramPreferences prefs;
	private int vertexSize;
	Vec2[] triV2 = new Vec2[3];

	public UVViewportModelRenderer() {
		prefs = ProgramGlobals.getPrefs();
		vertexSize = prefs.getVertexSize();
	}

	public void drawGeosetUVs(Graphics2D graphics,
	                          CoordinateSystem coordinateSystem,
	                          ModelHandler modelHandler) {
		ModelView modelView = modelHandler.getModelView();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			graphics.setColor(ProgramGlobals.getPrefs().getTriangleColor());
			if (modelView.getHighlightedGeoset() == geoset) {
				graphics.setColor(ProgramGlobals.getPrefs().getHighlighTriangleColor());
			}
			for (Triangle triangle : geoset.getTriangles()) {
				renderFace(graphics, coordinateSystem, modelView, triangle);
//				int vIndex = 0;
//				for (GeosetVertex vertex : triangle.getVerts()) {
//
//					triV2[vIndex] = CoordSysUtils.convertToViewVec2(coordinateSystem, vertex.getTVertex(0));
//
//					vIndex++;
//				}
//				GU.drawPolygon(graphics, triV2);
			}
			for (GeosetVertex vertex : geoset.getVertices()){
				renderVertex(graphics, coordinateSystem, modelView, vertex);
			}
		}
	}

	public void renderFace(Graphics2D graphics,
	                       CoordinateSystem coordinateSystem,
	                       ModelView modelView,
	                       Triangle triangle) {
		Vec2 pointA = CoordSysUtils.convertToViewVec2(coordinateSystem, triangle.get(0).getTVertex(0));
		Vec2 pointB = CoordSysUtils.convertToViewVec2(coordinateSystem, triangle.get(1).getTVertex(0));
		Vec2 pointC = CoordSysUtils.convertToViewVec2(coordinateSystem, triangle.get(2).getTVertex(0));

		if(triangleSelected(modelView, triangle)){
			graphics.setColor(FACE_SELECTED_COLOR);
		} else {
			graphics.setColor(FACE_NOT_SELECTED_COLOR);
		}
		GU.fillPolygon(graphics, pointA, pointB, pointC);

		if(triangleSelected(modelView, triangle)){
			graphics.setColor(prefs.getSelectColor());
		} else {
			graphics.setColor(prefs.getTriangleColor());
		}
		GU.drawPolygon(graphics, pointA, pointB, pointC);
	}

	private boolean triangleSelected(ModelView modelView, Triangle triangle) {
		return modelView.isSelected(triangle.get(0)) && modelView.isSelected(triangle.get(1)) && modelView.isSelected(triangle.get(2));
	}

	public void renderVertex(Graphics2D graphics,
	                         CoordinateSystem coordinateSystem,
	                         ModelView modelView,
	                         GeosetVertex vertex) {
		Vec2 pointA = CoordSysUtils.convertToViewVec2(coordinateSystem, vertex.getTVertex(0));
		if(modelView.isSelected(vertex)){
			graphics.setColor(prefs.getSelectColor());
		} else {
			graphics.setColor(prefs.getVertexColor());
		}
		GU.fillCenteredSquare(graphics, pointA, vertexSize);
//		graphics.fillRect(pointA.x - (vertexSize / 2), (int) (pointA.y - (vertexSize / 2.0)), vertexSize, vertexSize);
	}
}
