package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.TimelineContainer;

/**
 * Removes a whole track from its container, leaving the static value in charge
 * again ("Make Static" in the component cards).
 */
public class RemoveTimelineAction implements UndoAction {
	private final AnimFlag timeline;
	private final TimelineContainer container;
	private final ModelStructureChangeListener structureChangeListener;

	public RemoveTimelineAction(final TimelineContainer container, final AnimFlag timeline,
			final ModelStructureChangeListener structureChangeListener) {
		this.container = container;
		this.timeline = timeline;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public void undo() {
		container.add(timeline);
		structureChangeListener.timelineAdded(container, timeline);
	}

	@Override
	public void redo() {
		container.remove(timeline);
		structureChangeListener.timelineRemoved(container, timeline);
	}

	@Override
	public String actionName() {
		return "remove " + timeline.getName() + " track";
	}
}
