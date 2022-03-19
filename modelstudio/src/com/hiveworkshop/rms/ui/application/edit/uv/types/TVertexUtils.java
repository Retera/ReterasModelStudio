package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.util.Vec2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public enum TVertexUtils {
	;

	public static Collection<Vec2> getTVertices(Collection<GeosetVertex> vertexSelection, int uvLayerIndex) {
		List<Vec2> tVertices = new ArrayList<>();
		for (GeosetVertex vertex : vertexSelection) {
			if (uvLayerIndex < vertex.getTverts().size()) {
				tVertices.add(vertex.getTVertex(uvLayerIndex));
			}
		}
		return tVertices;
	}
}
