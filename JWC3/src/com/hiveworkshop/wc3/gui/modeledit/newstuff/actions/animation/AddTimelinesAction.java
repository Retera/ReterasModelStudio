package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation;

import java.util.Collection;

import com.etheller.util.Pair;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.TimelineContainer;

public class AddTimelinesAction implements UndoAction {
	private final Collection<Pair<TimelineContainer, AnimFlag>> containersAndTimelines;

	public AddTimelinesAction(final Collection<Pair<TimelineContainer, AnimFlag>> containersAndTimelines) {
		this.containersAndTimelines = containersAndTimelines;
	}

	@Override
	public void undo() {
		for (final Pair<TimelineContainer, AnimFlag> pair : containersAndTimelines) {
			pair.getFirst().remove(pair.getSecond());
		}
	}

	@Override
	public void redo() {
		for (final Pair<TimelineContainer, AnimFlag> pair : containersAndTimelines) {
			pair.getFirst().add(pair.getSecond());
		}
	}

	@Override
	public String actionName() {
		return "add timeline";
	}

}
