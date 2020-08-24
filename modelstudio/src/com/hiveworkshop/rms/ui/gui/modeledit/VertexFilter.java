package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.Vertex;

public interface VertexFilter<TYPE extends Vertex> {
	boolean isAccepted(TYPE vertex);

	VertexFilter<Vertex> IDENTITY = new VertexFilter<Vertex>() {
		@Override
		public boolean isAccepted(final Vertex vertex) {
			return true;
		}
	};
}
