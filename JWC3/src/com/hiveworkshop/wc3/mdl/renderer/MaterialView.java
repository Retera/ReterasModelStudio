package com.hiveworkshop.wc3.mdl.renderer;

import java.util.List;

public interface MaterialView {
	List<? extends LayerView> getLayers();

	int getPriorityPlane();

	String getName(); // TODO should probably be a util

	boolean isConstantColor();

	boolean isSortPrimsFarZ();

	boolean isFullResolution();
}
