package com.hiveworkshop.wc3.mdl.v2.timelines.concept;

public interface InterpolationCallback<KEY_TYPE, RETURN_TYPE> {
	RETURN_TYPE frame(int keyframeIndex, KEY_TYPE interpolatedKey);
}
