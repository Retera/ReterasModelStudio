package com.hiveworkshop.wc3.mdl.v2.visitor;

public interface VertexVisitor {
	void textureCoords(double u, double v);

	void vertexFinished();

	public static final VertexVisitor NO_ACTION = new VertexVisitor() {
		@Override
		public void textureCoords(final double u, final double v) {
		}

		@Override
		public void vertexFinished() {
		}
	};
}
