package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;

public final class MakeEditableAction implements UndoAction {
	private final EditabilityToggleHandler editabilityToggleHandler;

	public MakeEditableAction(final EditabilityToggleHandler editabilityToggleHandler) {
		this.editabilityToggleHandler = editabilityToggleHandler;
	}

	@Override
	public UndoAction undo() {
		editabilityToggleHandler.makeNotEditable();
		return this;
	}

	@Override
	public UndoAction redo() {
		editabilityToggleHandler.makeEditable();
		return this;
	}

	@Override
	public String actionName() {
		return "toggle visibility";
	}

}
