package com.hiveworkshop.wc3.gui.animedit;

import com.etheller.util.SubscriberSetNotifier;

public interface TimeSliderTimeListener {
	void timeChanged(int currentTime);

	public static final class TimeSliderTimeNotifier extends SubscriberSetNotifier<TimeSliderTimeListener>
			implements TimeSliderTimeListener {
		@Override
		public void timeChanged(final int currentTime) {
			for (final TimeSliderTimeListener listener : set) {
				listener.timeChanged(currentTime);
			}
		}
	}
}
