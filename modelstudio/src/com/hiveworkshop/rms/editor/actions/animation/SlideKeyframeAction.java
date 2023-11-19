package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

public class SlideKeyframeAction<T> implements UndoAction {
	private final int startTrackTime;
	private int endTime;
	private final Sequence sequence;
	private final AnimFlag<T> timeline;
	private Entry<T> tempEntry;
	private final Runnable keyframeChangeCallback;

	public SlideKeyframeAction(int startTrackTime, int endTrackTime, AnimFlag<T> timeline, Sequence sequence, Runnable keyframeChangeCallback) {
		this.startTrackTime = startTrackTime;
		this.endTime = endTrackTime;
		this.sequence = sequence;
		this.timeline = timeline;
		this.keyframeChangeCallback = keyframeChangeCallback;
	}

	public SlideKeyframeAction(int startTrackTime, AnimFlag<T> timeline, Sequence sequence, Runnable keyframeChangeCallback) {
		this.startTrackTime = startTrackTime;
		this.endTime = startTrackTime;
		this.sequence = sequence;
		this.timeline = timeline;
		this.keyframeChangeCallback = keyframeChangeCallback;
	}

	@Override
	public SlideKeyframeAction<T> undo() {
		slideKeyframe(endTime, startTrackTime);

		if (keyframeChangeCallback != null) {
			keyframeChangeCallback.run();
		}
		return this;
	}

	@Override
	public SlideKeyframeAction<T> redo() {
		slideKeyframe(startTrackTime, endTime);

		if (keyframeChangeCallback != null) {
			keyframeChangeCallback.run();
		}
		return this;
	}

	public SlideKeyframeAction<T> update(int newTime) {
		int lastTime = endTime;
		endTime = newTime;

		slideKeyframe(lastTime, endTime);
		return this;
	}

	@Override
	public String actionName() {
		return "Slide Keyframe";
	}


	private void slideKeyframe(int slideStart, int slideEnd) {
		if (slideStart != slideEnd) {
			Entry<T> entryToSlide = timeline.removeKeyframe(slideStart, sequence);
			if (entryToSlide != null) {
				Entry<T> entryAt = timeline.removeKeyframe(slideEnd, sequence);
				timeline.addEntry(slideEnd, entryToSlide, sequence);
				if (tempEntry != null) {
					timeline.addEntry(tempEntry, sequence);
				}
				tempEntry = entryAt;
			}
		}
	}
}
