package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.util.Vector3;

public interface VertexFilter<TYPE extends Vector3> {
	boolean isAccepted(TYPE vertex);

	VertexFilter<Vector3> IDENTITY = new VertexFilter<Vector3>() {
		@Override
		public boolean isAccepted(final Vector3 vertex) {
			return true;
		}
	};
}
