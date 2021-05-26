package com.hiveworkshop.rms.ui.application.edit.animation;

import java.util.HashSet;
import java.util.Set;

public interface TimeSliderTimeListener {
	void timeChanged(int currentTime);
	Set<TimeSliderTimeListener> listenerSet = new HashSet<>();

	final class TimeSliderTimeNotifier implements TimeSliderTimeListener {

		public void subscribe(final TimeSliderTimeListener listener) {
			listenerSet.add(listener);
		}

		public void unsubscribe(final TimeSliderTimeListener listener) {
			listenerSet.remove(listener);
		}
		@Override
		public void timeChanged(int currentTime) {
			for (TimeSliderTimeListener listener : listenerSet) {
				listener.timeChanged(currentTime);
			}
		}
	}
}
