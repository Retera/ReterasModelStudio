package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class AddTimelineAction implements UndoAction {
	private final AnimFlag<?> timeline;
	private final TimelineContainer container;
	private final ModelStructureChangeListener structureChangeListener;

	public AddTimelineAction(final TimelineContainer container, final AnimFlag<?> timeline,
	                         final ModelStructureChangeListener structureChangeListener) {
		this.container = container;
		this.timeline = timeline;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public void undo() {
		container.remove(timeline);
		structureChangeListener.timelineRemoved(container, timeline);
	}

	@Override
	public void redo() {
		container.add(timeline);
		structureChangeListener.timelineAdded(container, timeline);
	}

	@Override
	public String actionName() {
		return "add timeline";
	}

}
