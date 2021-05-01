package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;

import java.awt.*;

public class UVVPGeosetRendererImpl extends GeosetVisitor {
	private final UVVPTriangleRendererImpl triangleRenderer = new UVVPTriangleRendererImpl();
	private Graphics2D graphics;
	private CoordinateSystem coordinateSystem;
	private int uvLayerIndex;

	public UVVPGeosetRendererImpl reset(Graphics2D graphics, CoordinateSystem coordinateSystem, int uvLayerIndex) {
		this.graphics = graphics;
		this.coordinateSystem = coordinateSystem;
		this.uvLayerIndex = uvLayerIndex;
		return this;
	}

	@Override
	public TriangleVisitor beginTriangle() {
		return triangleRenderer.reset(graphics, coordinateSystem, uvLayerIndex);
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
