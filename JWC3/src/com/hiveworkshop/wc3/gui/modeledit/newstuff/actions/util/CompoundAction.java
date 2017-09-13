package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util;

import com.etheller.collections.ListView;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;

public final class CompoundAction implements UndoAction {
	private final ListView<? extends UndoAction> actions;
	private final String name;

	public CompoundAction(final String name, final ListView<? extends UndoAction> actions) {
		this.name = name;
		this.actions = actions;
	}

	@Override
	public void undo() {
		for (final UndoAction action : actions) {
			action.undo();
		}
	}

	@Override
	public void redo() {
		for (final UndoAction action : actions) {
			action.redo();
		}
	}

	@Override
	public String actionName() {
		return name;
	}

}
