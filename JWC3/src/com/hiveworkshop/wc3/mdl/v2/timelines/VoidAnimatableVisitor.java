package com.hiveworkshop.wc3.mdl.v2.timelines;

public interface VoidAnimatableVisitor<KEY_TYPE> {
	void staticValue(KEY_TYPE value);

	void animatedValues(Timeline<KEY_TYPE> timeline);

	void animatedTangentialValues(Timeline<TangentialKeyFrame<KEY_TYPE>> timeline);
}
