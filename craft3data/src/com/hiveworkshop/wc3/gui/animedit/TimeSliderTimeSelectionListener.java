package com.hiveworkshop.wc3.gui.animedit;

import java.util.List;
import java.util.Set;

import com.etheller.util.SubscriberSetNotifier;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.IdObject;

public interface TimeSliderTimeSelectionListener {
	void timeChanged(int currentTime, Set<IdObject> objects, List<AnimFlag> timelines);

	public static final class TimeSliderTimeNotifier extends SubscriberSetNotifier<TimeSliderTimeSelectionListener>
			implements TimeSliderTimeSelectionListener {
		@Override
		public void timeChanged(final int currentTime, final Set<IdObject> objects, final List<AnimFlag> timelines) {
			for (final TimeSliderTimeSelectionListener listener : set) {
				listener.timeChanged(currentTime, objects, timelines);
			}
		}
	}
}
