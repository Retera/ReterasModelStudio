package com.hiveworkshop.rms.editor.model.visitor;

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
