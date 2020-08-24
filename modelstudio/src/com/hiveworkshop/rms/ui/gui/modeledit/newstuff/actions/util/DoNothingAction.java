package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public final class DoNothingAction implements UndoAction {
	private final String actionName;

	public DoNothingAction(final String actionName) {
		this.actionName = actionName;
	}

	@Override
	public void undo() {

	}

	@Override
	public void redo() {

	}

	@Override
	public String actionName() {
		return actionName;
	}

}
