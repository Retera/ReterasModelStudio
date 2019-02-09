package com.hiveworkshop.wc3.mdl.v2.visitor;

import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.v2.MaterialView;

public interface MeshVisitor {
	GeosetVisitor beginGeoset(int geosetId, MaterialView material, GeosetAnim geosetAnim);
}
