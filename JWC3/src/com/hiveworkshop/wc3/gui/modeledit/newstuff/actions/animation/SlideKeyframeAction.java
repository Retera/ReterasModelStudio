package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation;

import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.AnimFlag;

public class SlideKeyframeAction implements UndoAction {
	private final int startTrackTime;
	private final int endTrackTime;
	private final List<AnimFlag> timelines;

	public SlideKeyframeAction(final int startTrackTime, final int endTrackTime, final List<AnimFlag> timelines) {
		this.startTrackTime = startTrackTime;
		this.endTrackTime = endTrackTime;
		this.timelines = timelines;
	}

	@Override
	public void undo() {
		for (final AnimFlag timeline : timelines) {
			timeline.slideKeyframe(endTrackTime, startTrackTime);
		}
	}

	@Override
	public void redo() {
		for (final AnimFlag timeline : timelines) {
			timeline.slideKeyframe(startTrackTime, endTrackTime);
		}
	}

	@Override
	public String actionName() {
		return "slide keyframe";
	}

}
