package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.*;

public class AnimVPGeosetRendererImpl extends GeosetVisitor {
	RenderModel renderModel;
	private Graphics2D graphics;
	private ProgramPreferences programPreferences;
	private CoordinateSystem coordinateSystem;
	private AnimVPTriangleRendererImpl triangleRenderer;

	public AnimVPGeosetRendererImpl() {
		triangleRenderer = new AnimVPTriangleRendererImpl();
	}

	public AnimVPGeosetRendererImpl reset(Graphics2D graphics, ProgramPreferences programPreferences, CoordinateSystem coordinateSystem, RenderModel renderModel) {
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.coordinateSystem = coordinateSystem;
		this.renderModel = renderModel;
		return this;
	}

	@Override
	public TriangleVisitor beginTriangle() {
		return triangleRenderer.reset(graphics, programPreferences, coordinateSystem, renderModel);
	}

	private void renderGeosetTries(Geoset geoset, boolean isHD) {
		for (Triangle triangle : geoset.getTriangles()) {
			TriangleVisitor triangleRenderer = beginTriangle();
			for (GeosetVertex vertex : triangle.getVerts()) {
				triangleRenderer.vertex(vertex, isHD);
			}
			triangleRenderer.triangleFinished();
		}
	}
}