package com.hiveworkshop.rms.ui.application.edit.animation;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class TimeSliderTimeListener {

	Set<Consumer<Integer>> listenerSet = new HashSet<>();

	public void subscribe(Consumer<Integer> listener) {
		listenerSet.add(listener);
	}

	public void timeChanged(int currentTime) {

		for (Consumer<Integer> listener : listenerSet) {
			listener.accept(currentTime);
		}
	}

}
