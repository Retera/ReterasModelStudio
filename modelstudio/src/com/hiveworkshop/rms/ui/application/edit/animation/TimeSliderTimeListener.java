package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.util.SubscriberSetNotifier;

public interface TimeSliderTimeListener {
	void timeChanged(int currentTime);

	final class TimeSliderTimeNotifier extends SubscriberSetNotifier<TimeSliderTimeListener>
			implements TimeSliderTimeListener {
		@Override
		public void timeChanged(final int currentTime) {
			for (final TimeSliderTimeListener listener : set) {
				listener.timeChanged(currentTime);
			}
		}
	}
}
