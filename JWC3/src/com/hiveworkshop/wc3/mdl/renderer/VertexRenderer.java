package com.hiveworkshop.wc3.mdl.renderer;

public interface VertexRenderer {
	void textureCoords(double u, double v);

	void vertexFinished();

	public static final VertexRenderer NO_ACTION = new VertexRenderer() {
		@Override
		public void textureCoords(final double u, final double v) {
		}

		@Override
		public void vertexFinished() {
		}
	};
}
