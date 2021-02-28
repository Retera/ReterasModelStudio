package com.hiveworkshop.rms.ui.application.edit.animation;

public interface TimeBoundProvider {

	void addChangeListener(TimeBoundChangeListener listener);

	int getStart();

	int getEnd();
}
