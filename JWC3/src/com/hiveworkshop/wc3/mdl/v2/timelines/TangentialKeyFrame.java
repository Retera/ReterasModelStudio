package com.hiveworkshop.wc3.mdl.v2.timelines;

public interface TangentialKeyFrame<KEY_TYPE> {
	KEY_TYPE getValue();

	KEY_TYPE getInTan();

	KEY_TYPE getOutTan();
}
