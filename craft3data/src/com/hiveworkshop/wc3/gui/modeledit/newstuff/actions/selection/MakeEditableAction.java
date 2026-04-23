package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.selection;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.matrixeater.localization.LocalizationManager;

public final class MakeEditableAction implements UndoAction {
	private final EditabilityToggleHandler editabilityToggleHandler;

	public MakeEditableAction(final EditabilityToggleHandler editabilityToggleHandler) {
		this.editabilityToggleHandler = editabilityToggleHandler;
	}

	@Override
	public void undo() {
		editabilityToggleHandler.makeNotEditable();
	}

	@Override
	public void redo() {
		editabilityToggleHandler.makeEditable();
	}

	@Override
	public String actionName() {
		return LocalizationManager.getInstance().get("string.makeeditableaction_actionname");
	}

}
