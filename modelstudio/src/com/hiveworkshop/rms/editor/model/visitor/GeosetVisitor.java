package com.hiveworkshop.rms.editor.model.visitor;


public class GeosetVisitor {
	public static GeosetVisitor NO_ACTION = new GeosetVisitor();

	public TriangleVisitor beginTriangle() {
		return TriangleVisitor.NO_ACTION;
	}
}