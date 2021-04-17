package com.hiveworkshop.rms.editor.model.visitor;

public interface VertexVisitor {
	void textureCoords(double u, double v);

	void vertexFinished();

	VertexVisitor NO_ACTION = new VertexVisitor() {
		@Override
		public void textureCoords(final double u, final double v) {
		}

		@Override
		public void vertexFinished() {
		}
	};
}
