package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class ReversedAction implements UndoAction {
	private final UndoAction delegate;
	private final String actionName;

	public ReversedAction(final String actionName, final UndoAction delegate) {
		this.actionName = actionName;
		this.delegate = delegate;
	}

	@Override
	public UndoAction undo() {
		delegate.redo();
		return this;
	}

	@Override
	public UndoAction redo() {
		delegate.undo();
		return this;
	}

	@Override
	public String actionName() {
		return actionName;
	}

}
