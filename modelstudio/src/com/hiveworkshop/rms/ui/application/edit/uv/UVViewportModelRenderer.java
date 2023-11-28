package com.hiveworkshop.rms.ui.application.edit.uv;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.ui.preferences.EditorColorPrefs;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.ImageUtils.GU;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;

public class UVViewportModelRenderer {
	private static final Color FACE_SELECTED_COLOR = new Color(1f, 0.45f, 0.45f, 0.3f);
	private static final Color FACE_HIGHLIGHT_COLOR = new Color(0.45f, 1f, 0.45f, 0.3f);
	private static final Color FACE_NOT_SELECTED_COLOR = new Color(0.45f, 0.45f, 1f, 0.3f);
	ProgramPreferences prefs;
	EditorColorPrefs colorPrefs;
	private int vertexSize;
	Vec2[] triV2 = new Vec2[] {new Vec2(), new Vec2(), new Vec2()};
	private int uvLayer;

	public UVViewportModelRenderer() {
		prefs = ProgramGlobals.getPrefs();
		vertexSize = prefs.getVertexSize();
		colorPrefs = ProgramGlobals.getEditorColorPrefs();
	}

	public void setUvLayer(int uvLayer) {
		this.uvLayer = uvLayer;
	}

	public void drawGeosetUVs(Graphics2D graphics,
	                          CoordinateSystem coordinateSystem,
	                          ModelHandler modelHandler) {
		ModelView modelView = modelHandler.getModelView();
		for (Geoset geoset : modelView.getVisibleGeosets()) {
			graphics.setColor(colorPrefs.getColor(ColorThing.TRIANGLE_AREA));
			if (modelView.getHighlightedGeoset() == geoset) {
				graphics.setColor(colorPrefs.getColor(ColorThing.TRIANGLE_AREA_HIGHLIGHTED));
			}
			for (Triangle triangle : geoset.getTriangles()) {
				if (!triangleHidden(modelView, triangle)) {
					renderFace(graphics, coordinateSystem, modelView, triangle);
				}
			}
			for (GeosetVertex vertex : geoset.getVertices()) {
				if (!modelView.isHidden(vertex)) {
					renderVertex(graphics, coordinateSystem, modelView, vertex);
				}
			}
		}
	}

	public void renderFace(Graphics2D graphics,
	                       CoordinateSystem coordinateSystem,
	                       ModelView modelView,
	                       Triangle triangle) {
		triV2[0].set(convertToViewVec2(coordinateSystem, triangle.get(0).getTVertex(uvLayer)));
		triV2[1].set(convertToViewVec2(coordinateSystem, triangle.get(1).getTVertex(uvLayer)));
		triV2[2].set(convertToViewVec2(coordinateSystem, triangle.get(2).getTVertex(uvLayer)));

		if (modelView.isEditable(triangle)) {
			if (triangleSelected(modelView, triangle)) {
				graphics.setColor(colorPrefs.getColor(ColorThing.TRIANGLE_AREA_SELECTED));
			} else {
				graphics.setColor(colorPrefs.getColor(ColorThing.TRIANGLE_AREA));
			}
		} else {
			graphics.setColor(colorPrefs.getColor(ColorThing.TRIANGLE_AREA_UNEDITABLE));
		}
		GU.fillPolygon(graphics, triV2);

		if (triangleSelected(modelView, triangle)) {
			graphics.setColor(colorPrefs.getColor(ColorThing.TRIANGLE_LINE_SELECTED));
		} else {
			graphics.setColor(colorPrefs.getColor(ColorThing.TRIANGLE_LINE));
		}
		if (modelView.isEditable(triangle)) {
			if (triangleSelected(modelView, triangle)) {
				graphics.setColor(colorPrefs.getColor(ColorThing.TRIANGLE_LINE_SELECTED));
			} else {
				graphics.setColor(colorPrefs.getColor(ColorThing.TRIANGLE_LINE));
			}
		} else {
			graphics.setColor(colorPrefs.getColor(ColorThing.TRIANGLE_LINE_UNEDITABLE));
		}
		GU.drawPolygon(graphics, triV2);
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
		Vec2 pointA = convertToViewVec2(coordinateSystem, vertex.getTVertex(uvLayer));
		if (modelView.isEditable(vertex)) {
			if (modelView.isSelected(vertex)) {
				graphics.setColor(colorPrefs.getColor(ColorThing.VERTEX_SELECTED));
			} else {
				graphics.setColor(colorPrefs.getColor(ColorThing.VERTEX));
			}
		} else {
			graphics.setColor(colorPrefs.getColor(ColorThing.VERTEX_UNEDITABLE));
		}
		GU.fillCenteredSquare(graphics, pointA, vertexSize);
	}

	public Vec2 convertToViewVec2(CoordinateSystem coordinateSystem, Vec2 vertex) {
		return coordinateSystem.viewV(vertex == null ? Vec2.ORIGIN : vertex);
	}
}
