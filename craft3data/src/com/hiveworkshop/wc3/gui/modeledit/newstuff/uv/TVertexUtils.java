package com.hiveworkshop.wc3.gui.modeledit.newstuff.uv;

import java.util.ArrayList;
import java.util.Collection;

import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.TVertex;
import com.hiveworkshop.wc3.mdl.Vertex;

public enum TVertexUtils {
	;

	public static Collection<? extends TVertex> getTVertices(final Collection<? extends Vertex> vertexSelection,
			final int uvLayerIndex) {
		final ArrayList<TVertex> tVertices = new ArrayList<TVertex>();
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
