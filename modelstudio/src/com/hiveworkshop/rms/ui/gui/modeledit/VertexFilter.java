package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.util.Vertex3;

public interface VertexFilter<TYPE extends Vertex3> {
	boolean isAccepted(TYPE vertex);

	VertexFilter<Vertex3> IDENTITY = new VertexFilter<Vertex3>() {
		@Override
		public boolean isAccepted(final Vertex3 vertex) {
			return true;
		}
	};
}
