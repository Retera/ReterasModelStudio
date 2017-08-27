package com.hiveworkshop.wc3.mdl.v2.visitor;

public interface GeosetVisitor {
	TriangleVisitor beginTriangle();

	void geosetFinished();

	GeosetVisitor NO_ACTION = new GeosetVisitor() {
		@Override
		public void geosetFinished() {

		}

		@Override
		public TriangleVisitor beginTriangle() {
			return TriangleVisitor.NO_ACTION;
		}
	};
}
