package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.util.Vec3;

public interface VertexFilter<TYPE extends Vec3> {
	boolean isAccepted(TYPE vertex);

	VertexFilter<Vec3> IDENTITY = vertex -> true;
}
