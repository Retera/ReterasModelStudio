package com.hiveworkshop.wc3.mdl.v2.timelines;

public interface Animatable<KEY_TYPE> {
	<RESULT_TYPE> RESULT_TYPE visit(AnimatableVisitor<KEY_TYPE, RESULT_TYPE> visitor);

	void visit(VoidAnimatableVisitor<KEY_TYPE> visitor);
}
