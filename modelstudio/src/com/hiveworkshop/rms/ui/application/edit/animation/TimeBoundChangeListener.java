package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.util.SubscriberSetNotifier;

public interface TimeBoundChangeListener {
	void timeBoundsChanged(int start, int end);

	final class TimeBoundChangeNotifier extends SubscriberSetNotifier<TimeBoundChangeListener>
			implements TimeBoundChangeListener {
		@Override
		public void timeBoundsChanged(final int start, final int end) {
			for (final TimeBoundChangeListener listener : set) {
				listener.timeBoundsChanged(start, end);
			}
		}
	}
}
