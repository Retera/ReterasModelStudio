package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class DoNothingMoveActionAdapter implements GenericMoveAction {

	private final UndoAction delegate;

	public DoNothingMoveActionAdapter(final UndoAction delegate) {
		this.delegate = delegate;
	}

	@Override
	public void undo() {
		delegate.undo();
	}

	@Override
	public void redo() {
		delegate.redo();
	}

	@Override
	public String actionName() {
		return delegate.actionName();
	}

	@Override
	public void updateTranslation(final double deltaX, final double deltaY, final double deltaZ) {
	}

}
