package com.hiveworkshop.wc3.mdl.v2.visitor;

import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.Material;

public interface MeshVisitor {
	GeosetVisitor beginGeoset(int geosetId, Material material, GeosetAnim geosetAnim);
}
