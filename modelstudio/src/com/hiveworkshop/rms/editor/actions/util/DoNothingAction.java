package com.hiveworkshop.rms.editor.actions.util;

import com.hiveworkshop.rms.editor.actions.UndoAction;

public final class DoNothingAction implements UndoAction {
	private final String actionName;

	public DoNothingAction(final String actionName) {
		this.actionName = actionName;
	}

	@Override
	public UndoAction undo() {
		return this;
	}

	@Override
	public UndoAction redo() {
		return this;
	}

	@Override
	public String actionName() {
		return actionName;
	}

}
