package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.List;

public class SlideKeyframeAction implements UndoAction {
	private final int startTrackTime;
	private final int endTrackTime;
	private final List<AnimFlag<?>> timelines;
	private final Runnable keyframeChangeCallback;

	public SlideKeyframeAction(int startTrackTime, int endTrackTime, List<AnimFlag<?>> timelines, Runnable keyframeChangeCallback) {
		this.startTrackTime = startTrackTime;
		this.endTrackTime = endTrackTime;
		this.timelines = timelines;
		this.keyframeChangeCallback = keyframeChangeCallback;
	}

	@Override
	public void undo() {
		for (final AnimFlag<?> timeline : timelines) {
			timeline.slideKeyframe(endTrackTime, startTrackTime);
		}
		keyframeChangeCallback.run();
	}

	@Override
	public void redo() {
		for (final AnimFlag<?> timeline : timelines) {
			timeline.slideKeyframe(startTrackTime, endTrackTime);
		}
		keyframeChangeCallback.run();
	}

	@Override
	public String actionName() {
		return "slide keyframe";
	}

}
