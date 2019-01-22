package com.hiveworkshop.wc3.mdl.v2.timelines;

public interface AnimatableVisitor<KEY_TYPE, RESULT_TYPE> {
	RESULT_TYPE staticValue(KEY_TYPE value);

	RESULT_TYPE animatedValues(Timeline<KEY_TYPE> timeline);

	RESULT_TYPE animatedTangentialValues(Timeline<TangentialKeyFrame<KEY_TYPE>> timeline);
}
