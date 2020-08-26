package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.util.Vector2;
import com.hiveworkshop.rms.util.Vector3;

public enum TVertexUtils {
	;

	public static Collection<? extends Vector2> getTVertices(final Collection<? extends Vector3> vertexSelection,
			final int uvLayerIndex) {
		final List<Vector2> tVertices = new ArrayList<Vector2>();
		for (final Vector3 vertex : vertexSelection) {
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
