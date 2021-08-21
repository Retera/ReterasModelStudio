package com.hiveworkshop.rms.ui.application.edit.animation;

public interface TimeBoundProvider extends Comparable<TimeBoundProvider> {

	int getStart();

	int getEnd();
}
