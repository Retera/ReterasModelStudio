package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.List;

public class SlideKeyframeAction implements UndoAction {
	private final int startTrackTime;
	private final int endTrackTime;
	private final Sequence sequence;
	private final List<AnimFlag<?>> timelines;
	private final Runnable keyframeChangeCallback;

	public SlideKeyframeAction(int startTrackTime, int endTrackTime, List<AnimFlag<?>> timelines, Sequence sequence, Runnable keyframeChangeCallback) {
		this.startTrackTime = startTrackTime;
		this.endTrackTime = endTrackTime;
		this.sequence = sequence;
		this.timelines = timelines;
		this.keyframeChangeCallback = keyframeChangeCallback;
	}

	@Override
	public UndoAction undo() {
		for (final AnimFlag<?> timeline : timelines) {
			timeline.slideKeyframe(endTrackTime, startTrackTime, sequence);
		}
		if (keyframeChangeCallback != null) {
			keyframeChangeCallback.run();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (final AnimFlag<?> timeline : timelines) {
			timeline.slideKeyframe(startTrackTime, endTrackTime, sequence);
		}
		if (keyframeChangeCallback != null) {
			keyframeChangeCallback.run();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "slide keyframe";
	}

}
