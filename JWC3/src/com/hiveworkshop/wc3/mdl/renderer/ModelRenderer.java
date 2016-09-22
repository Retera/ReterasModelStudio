package com.hiveworkshop.wc3.mdl.renderer;

import com.hiveworkshop.wc3.mdl.GeosetAnim;

public interface ModelRenderer extends IdObjectVisitor {
	GeosetRenderer beginGeoset(MaterialView material, GeosetAnim geosetAnim);

}
