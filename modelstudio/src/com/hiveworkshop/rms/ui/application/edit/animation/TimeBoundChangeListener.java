package com.hiveworkshop.rms.ui.application.edit.animation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class TimeBoundChangeListener {

	Set<TimeSliderPanel> listenerSet = new HashSet<>();
	Map<Object, BiConsumer<Integer, Integer>> listenerSet2 = new HashMap<>();

	public void subscribe(final TimeSliderPanel listener) {
		listenerSet.add(listener);
	}

	public void unsubscribe(final TimeSliderPanel listener) {
		listenerSet.remove(listener);
	}

	public void subscribe(Object owner, BiConsumer<Integer, Integer> listener) {
		listenerSet2.put(owner, listener);
	}

	public void unsubscribe(Object owner) {
		listenerSet2.remove(owner);
	}

	public void timeBoundsChanged(int start, int end) {
		for (TimeSliderPanel listener : listenerSet) {
			listener.timeBoundsChanged(start, end);
		}
		for (BiConsumer<Integer, Integer> listener : listenerSet2.values()) {
			listener.accept(start, end);
		}
	}
}
