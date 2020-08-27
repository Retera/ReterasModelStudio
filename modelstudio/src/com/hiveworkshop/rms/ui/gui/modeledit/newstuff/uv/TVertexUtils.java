package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

public enum TVertexUtils {
	;

	public static Collection<? extends Vec2> getTVertices(final Collection<? extends Vec3> vertexSelection,
			final int uvLayerIndex) {
		final List<Vec2> tVertices = new ArrayList<Vec2>();
		for (final Vec3 vertex : vertexSelection) {
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
