package com.hiveworkshop.rms.ui.application.edit.animation;

import java.util.HashSet;
import java.util.Set;

public class TimeBoundChangeListener {

	Set<TimeSliderPanel> listenerSet = new HashSet<>();

	public void subscribe(final TimeSliderPanel listener) {
		listenerSet.add(listener);
	}

	public void unsubscribe(final TimeSliderPanel listener) {
		listenerSet.remove(listener);
	}

	public void timeBoundsChanged(int start, int end) {
		for (TimeSliderPanel listener : listenerSet) {
			listener.timeBoundsChanged(start, end);
		}
	}
}
