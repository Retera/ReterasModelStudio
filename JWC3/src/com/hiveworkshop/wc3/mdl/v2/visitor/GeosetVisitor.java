package com.hiveworkshop.wc3.mdl.v2.visitor;

public interface GeosetVisitor {
	TriangleVisitor beginTriangle();

	void geosetFinished();
}
