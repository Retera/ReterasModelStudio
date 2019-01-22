package com.hiveworkshop.wc3.gui.modeledit;

import com.hiveworkshop.wc3.mdl.Vertex;

public interface VertexFilter<TYPE extends Vertex> {
	boolean isAccepted(TYPE vertex);

	public VertexFilter<Vertex> IDENTITY = new VertexFilter<Vertex>() {
		@Override
		public boolean isAccepted(final Vertex vertex) {
			return true;
		}
	};
}
