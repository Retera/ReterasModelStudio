package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.util.SubscriberSetNotifier;

import java.util.List;
import java.util.Set;

public interface TimeSliderTimeSelectionListener {
	void timeChanged(int currentTime, Set<IdObject> objects, List<AnimFlag<?>> timelines);

	final class TimeSliderTimeNotifier extends SubscriberSetNotifier<TimeSliderTimeSelectionListener>
			implements TimeSliderTimeSelectionListener {
		@Override
		public void timeChanged(final int currentTime, final Set<IdObject> objects, final List<AnimFlag<?>> timelines) {
			for (final TimeSliderTimeSelectionListener listener : set) {
				listener.timeChanged(currentTime, objects, timelines);
			}
		}
	}
}
