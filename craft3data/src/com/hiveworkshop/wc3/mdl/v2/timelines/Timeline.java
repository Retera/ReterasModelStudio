package com.hiveworkshop.wc3.mdl.v2.timelines;

import java.util.HashMap;

public interface Timeline<KEY_TYPE> {
	InterpolationType getInterpolationType();

	HashMap<Integer, KEY_TYPE> getTimeToKey();
}
