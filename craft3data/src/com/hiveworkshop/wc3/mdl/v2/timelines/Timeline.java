package com.hiveworkshop.wc3.mdl.v2.timelines;

import java.util.HashMap;

import com.etheller.warsmash.parsers.mdlx.InterpolationType;

public interface Timeline<KEY_TYPE> {
	InterpolationType getInterpolationType();

	HashMap<Integer, KEY_TYPE> getTimeToKey();
}
