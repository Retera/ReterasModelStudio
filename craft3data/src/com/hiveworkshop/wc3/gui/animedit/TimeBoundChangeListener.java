package com.hiveworkshop.wc3.gui.animedit;

import com.etheller.util.SubscriberSetNotifier;

public interface TimeBoundChangeListener {
	void timeBoundsChanged(int start, int end);

	public static final class TimeBoundChangeNotifier extends SubscriberSetNotifier<TimeBoundChangeListener>
			implements TimeBoundChangeListener {
		@Override
		public void timeBoundsChanged(final int start, final int end) {
			for (final TimeBoundChangeListener listener : set) {
				listener.timeBoundsChanged(start, end);
			}
		}
	}
}
