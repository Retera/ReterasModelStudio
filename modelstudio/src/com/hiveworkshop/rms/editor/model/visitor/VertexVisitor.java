package com.hiveworkshop.rms.editor.model.visitor;

public interface VertexVisitor {
	void textureCoords(double u, double v);


	VertexVisitor NO_ACTION = (u, v) -> { };
}
