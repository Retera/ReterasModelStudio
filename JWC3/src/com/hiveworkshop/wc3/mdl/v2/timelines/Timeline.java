package com.hiveworkshop.wc3.mdl.v2.timelines;

import com.etheller.collections.SortedMapView;

public interface Timeline<KEY_TYPE> {
	InterpolationType getInterpolationType();

	SortedMapView<Integer, KEY_TYPE> getTimeToKey();
}
