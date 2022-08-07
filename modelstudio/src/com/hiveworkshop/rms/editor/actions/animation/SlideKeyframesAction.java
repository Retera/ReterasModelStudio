package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.ArrayList;
import java.util.List;

public class SlideKeyframesAction implements UndoAction {
	private final List<SlideKeyframeAction<?>> slideActions;

	private final Runnable keyframeChangeCallback;

	public SlideKeyframesAction(int startTrackTime, int endTrackTime, List<AnimFlag<?>> timelines, Sequence sequence, Runnable keyframeChangeCallback) {
		this.keyframeChangeCallback = keyframeChangeCallback;

		slideActions = new ArrayList<>();
		for (AnimFlag<?> timeline : timelines) {
			slideActions.add(new SlideKeyframeAction<>(startTrackTime, timeline, sequence, null).update(endTrackTime));
		}
	}

	public SlideKeyframesAction(int startTrackTime, List<AnimFlag<?>> timelines, Sequence sequence, Runnable keyframeChangeCallback) {
		this.keyframeChangeCallback = keyframeChangeCallback;

		slideActions = new ArrayList<>();
		for (AnimFlag<?> timeline : timelines) {
			slideActions.add(new SlideKeyframeAction<>(startTrackTime, timeline, sequence, null));
		}
	}

	@Override
	public UndoAction undo() {
		for (final SlideKeyframeAction<?> action : slideActions) {
			action.undo();
		}
		if (keyframeChangeCallback != null) {
			keyframeChangeCallback.run();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (final SlideKeyframeAction<?> action : slideActions) {
			action.redo();
		}
		if (keyframeChangeCallback != null) {
			keyframeChangeCallback.run();
		}
		return this;
	}

	public UndoAction update(int newTime) {
		for (final SlideKeyframeAction<?> action : slideActions) {
			action.update(newTime);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Slide Keyframe";
	}
}
