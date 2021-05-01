package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.*;

public class VPGeosetRendererImpl extends GeosetVisitor {
	private final VPTriangleRendererImpl triangleRenderer = new VPTriangleRendererImpl();
	private Graphics2D graphics;
	private ProgramPreferences programPreferences;
	private CoordinateSystem coordinateSystem;

	public VPGeosetRendererImpl() {

	}

	public VPGeosetRendererImpl reset(Graphics2D graphics, ProgramPreferences programPreferences, CoordinateSystem coordinateSystem) {
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.coordinateSystem = coordinateSystem;
		this.triangleRenderer.reset(graphics, programPreferences, coordinateSystem);
		return this;
	}

	@Override
	public TriangleVisitor beginTriangle() {
		return triangleRenderer.reset(graphics, programPreferences, coordinateSystem);
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