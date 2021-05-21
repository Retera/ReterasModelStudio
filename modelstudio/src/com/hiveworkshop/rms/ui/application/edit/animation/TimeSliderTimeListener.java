package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.util.SubscriberSetNotifier;

public interface TimeSliderTimeListener {
	void timeChanged(int currentTime);

	final class TimeSliderTimeNotifier extends SubscriberSetNotifier<TimeSliderTimeListener>
			implements TimeSliderTimeListener {
		@Override
		public void timeChanged(int currentTime) {
			for (TimeSliderTimeListener listener : listenerSet) {
				listener.timeChanged(currentTime);
			}
		}
	}
}
