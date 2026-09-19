package com.hiveworkshop.wc3.gui.modeledit.components.cards;

import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.GeosetVertexBoneLink;

public class ComponentBoneCard extends ComponentNodeCard<Bone> {
	@Override
	protected void addTypeRows() {
		addInfo("Skinned vertices", bone -> {
			int count = 0;
			for (final Geoset geoset : model().getGeosets()) {
				for (final GeosetVertex vertex : geoset.getVertices()) {
					for (final GeosetVertexBoneLink link : vertex.getLinks()) {
						if (link.bone == bone) {
							count++;
							break;
						}
					}
				}
			}
			return Integer.toString(count);
		});
	}
}
