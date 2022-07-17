package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.AnimFlag;

public class SlideKeyframeByIndexAction implements UndoAction {

	private final AnimFlag track;
	private final int keyIndex;
	private final int deltaTime;
	private final Runnable keyframeChangeCallback;

	public SlideKeyframeByIndexAction(final AnimFlag track, final int keyIndex, final int deltaTime,
			final Runnable keyframeChangeCallback) {
		this.track = track;
		this.keyIndex = keyIndex;
		this.deltaTime = deltaTime;
		this.keyframeChangeCallback = keyframeChangeCallback;
	}

	@Override
	public void undo() {
		final Integer time = track.getTimes().get(keyIndex);
		track.getTimes().set(keyIndex, time - deltaTime);
		keyframeChangeCallback.run();
	}

	@Override
	public void redo() {
		final Integer time = track.getTimes().get(keyIndex);
		track.getTimes().set(keyIndex, time + deltaTime);
		keyframeChangeCallback.run();
	}

	@Override
	public String actionName() {
		return "slide keyframe";
	}

}
