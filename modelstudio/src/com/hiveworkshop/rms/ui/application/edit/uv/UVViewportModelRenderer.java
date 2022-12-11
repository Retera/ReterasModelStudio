package com.hiveworkshop.rms.ui.application.edit.uv;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
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
		for (Geoset geoset : modelView.getVisibleGeosets()) {
			graphics.setColor(ProgramGlobals.getPrefs().getTriangleColor());
			if (modelView.getHighlightedGeoset() == geoset) {
				graphics.setColor(ProgramGlobals.getPrefs().getHighlighTriangleColor());
			}
			for (Triangle triangle : geoset.getTriangles()) {
				if(!triangleHidden(modelView, triangle)){
					renderFace(graphics, coordinateSystem, modelView, triangle);
				}
			}
			for (GeosetVertex vertex : geoset.getVertices()){
				if(!modelView.isHidden(vertex)){
					renderVertex(graphics, coordinateSystem, modelView, vertex);
				}
			}
		}
	}

	public void renderFace(Graphics2D graphics,
	                       CoordinateSystem coordinateSystem,
	                       ModelView modelView,
	                       Triangle triangle) {
		Vec2 pointA = convertToViewVec2(coordinateSystem, triangle.get(0).getTVertex(0));
		Vec2 pointB = convertToViewVec2(coordinateSystem, triangle.get(1).getTVertex(0));
		Vec2 pointC = convertToViewVec2(coordinateSystem, triangle.get(2).getTVertex(0));

		if(modelView.isEditable(triangle)){
			if (triangleSelected(modelView, triangle)) {
				graphics.setColor(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.TRIANGLE_AREA_SELECTED));
			} else {
				graphics.setColor(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.TRIANGLE_AREA));
			}
		} else {
			graphics.setColor(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.TRIANGLE_AREA_UNEDITABLE));
		}
		GU.fillPolygon(graphics, pointA, pointB, pointC);

		if (triangleSelected(modelView, triangle)) {
			graphics.setColor(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.TRIANGLE_LINE_SELECTED));
		} else {
			graphics.setColor(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.TRIANGLE_LINE));
		}
		if(modelView.isEditable(triangle)){
			if (triangleSelected(modelView, triangle)) {
				graphics.setColor(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.TRIANGLE_LINE_SELECTED));
			} else {
				graphics.setColor(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.TRIANGLE_LINE));
			}
		} else {
			graphics.setColor(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.TRIANGLE_LINE_UNEDITABLE));
		}
		GU.drawPolygon(graphics, pointA, pointB, pointC);
	}

	private boolean triangleSelected(ModelView modelView, Triangle triangle) {
		return modelView.isSelected(triangle.get(0)) && modelView.isSelected(triangle.get(1)) && modelView.isSelected(triangle.get(2));
	}

	private boolean triangleHidden(ModelView modelView, Triangle triangle) {
		return modelView.isHidden(triangle.get(0)) && modelView.isHidden(triangle.get(1)) && modelView.isHidden(triangle.get(2));
	}

	public void renderVertex(Graphics2D graphics,
	                         CoordinateSystem coordinateSystem,
	                         ModelView modelView,
	                         GeosetVertex vertex) {
		Vec2 pointA = convertToViewVec2(coordinateSystem, vertex.getTVertex(0));
		if (modelView.isEditable(vertex)){
			if (modelView.isSelected(vertex)) {
				graphics.setColor(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.VERTEX_SELECTED));
			} else {
				graphics.setColor(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.VERTEX));
			}
		} else {
			graphics.setColor(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.VERTEX_UNEDITABLE));
		}
		GU.fillCenteredSquare(graphics, pointA, vertexSize);
	}

	public static Vec2 convertToViewVec2(CoordinateSystem coordinateSystem, Vec2 vertex) {
		Vec2 pointA = coordinateSystem.viewVN(vertex);
		return pointA.set((1f +pointA.x)/2f * coordinateSystem.getParentWidth(), (1f-pointA.y)/2f * coordinateSystem.getParentHeight());
	}
}
