package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.TVertex;
import com.hiveworkshop.rms.util.Vertex;

public enum TVertexUtils {
	;

	public static Collection<? extends TVertex> getTVertices(final Collection<? extends Vertex> vertexSelection,
			final int uvLayerIndex) {
		final List<TVertex> tVertices = new ArrayList<TVertex>();
		for (final Vertex vertex : vertexSelection) {
			if (vertex instanceof GeosetVertex) {
				final GeosetVertex geosetVertex = (GeosetVertex) vertex;
				if (uvLayerIndex < geosetVertex.getTverts().size()) {
					tVertices.add(geosetVertex.getTVertex(uvLayerIndex));
				}
			}
		}
		return tVertices;
	}
}
