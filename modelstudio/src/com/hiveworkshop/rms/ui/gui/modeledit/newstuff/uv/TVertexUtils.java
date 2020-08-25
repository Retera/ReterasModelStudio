package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.util.Vertex2;
import com.hiveworkshop.rms.util.Vertex3;

public enum TVertexUtils {
	;

	public static Collection<? extends Vertex2> getTVertices(final Collection<? extends Vertex3> vertexSelection,
			final int uvLayerIndex) {
		final List<Vertex2> tVertices = new ArrayList<Vertex2>();
		for (final Vertex3 vertex : vertexSelection) {
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
