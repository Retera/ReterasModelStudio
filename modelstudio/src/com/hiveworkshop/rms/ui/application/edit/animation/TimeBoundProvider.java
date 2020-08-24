package com.hiveworkshop.rms.ui.application.edit.animation;

public interface TimeBoundProvider extends BasicTimeBoundProvider {

	void addChangeListener(TimeBoundChangeListener listener);
}
