package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.editor.model.Material;

public interface MeshVisitor {
	GeosetVisitor beginGeoset(int geosetId, Material material, GeosetAnim geosetAnim);
}
