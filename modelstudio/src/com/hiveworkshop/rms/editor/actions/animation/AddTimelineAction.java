package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;

public class AddTimelineAction implements UndoAction {
	private final AnimFlag<?> timeline;
	private final TimelineContainer container;

	public AddTimelineAction(TimelineContainer container, AnimFlag<?> timeline) {
		this.container = container;
		this.timeline = timeline;
	}

	@Override
	public UndoAction undo() {
		container.remove(timeline);
		return this;
	}

	@Override
	public UndoAction redo() {
		container.add(timeline);
		return this;
	}

	@Override
	public String actionName() {
		return "add timeline";
	}

}
