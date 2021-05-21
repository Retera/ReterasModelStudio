package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.util.SubscriberSetNotifier;

public interface TimeBoundChangeListener {
	void timeBoundsChanged(int start, int end);

	final class TimeBoundChangeNotifier extends SubscriberSetNotifier<TimeBoundChangeListener>
			implements TimeBoundChangeListener {
		@Override
		public void timeBoundsChanged(int start, int end) {
			for (TimeBoundChangeListener listener : listenerSet) {
				listener.timeBoundsChanged(start, end);
			}
		}
	}
}
