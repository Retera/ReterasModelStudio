package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Pair;

import java.util.Collection;

public class AddTimelinesAction implements UndoAction {
	private final Collection<Pair<TimelineContainer, AnimFlag<?>>> containersAndTimelines;

	public AddTimelinesAction(final Collection<Pair<TimelineContainer, AnimFlag<?>>> containersAndTimelines) {
		this.containersAndTimelines = containersAndTimelines;
	}

	@Override
	public void undo() {
		for (final Pair<TimelineContainer, AnimFlag<?>> pair : containersAndTimelines) {
			pair.getFirst().remove(pair.getSecond());
		}
	}

	@Override
	public void redo() {
		for (final Pair<TimelineContainer, AnimFlag<?>> pair : containersAndTimelines) {
			pair.getFirst().add(pair.getSecond());
		}
	}

	@Override
	public String actionName() {
		return "add timeline";
	}

}
