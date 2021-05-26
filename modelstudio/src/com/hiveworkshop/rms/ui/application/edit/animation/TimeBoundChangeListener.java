package com.hiveworkshop.rms.ui.application.edit.animation;

import java.util.HashSet;
import java.util.Set;

public interface TimeBoundChangeListener {
	void timeBoundsChanged(int start, int end);

	Set<TimeBoundChangeListener> listenerSet = new HashSet<>();

	final class TimeBoundChangeNotifier implements TimeBoundChangeListener {

		public void subscribe(final TimeBoundChangeListener listener) {
			listenerSet.add(listener);
		}

		public void unsubscribe(final TimeBoundChangeListener listener) {
			listenerSet.remove(listener);
		}

		@Override
		public void timeBoundsChanged(int start, int end) {
			for (TimeBoundChangeListener listener : listenerSet) {
				listener.timeBoundsChanged(start, end);
			}
		}
	}
}
