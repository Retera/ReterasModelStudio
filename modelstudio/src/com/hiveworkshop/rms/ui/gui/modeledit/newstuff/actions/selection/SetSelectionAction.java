package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;

import java.util.ArrayList;
import java.util.Collection;

public final class SetSelectionAction<T> implements UndoAction {

	private final Collection<T> selection;
	private final Collection<T> previousSelection;
	private final SelectionManager<T> selectionManager;
	private final String actionName;

	public SetSelectionAction(Collection<T> selection,
	                          Collection<T> previousSelection,
	                          SelectionManager<T> selectionManager,
	                          String actionName) {
		this.selection = new ArrayList<>(selection);
		this.previousSelection = new ArrayList<>(previousSelection);
		this.selectionManager = selectionManager;
		this.actionName = actionName;
	}

	@Override
	public void undo() {
		selectionManager.setSelection(previousSelection);

	}

	@Override
	public void redo() {
		selectionManager.setSelection(selection);
	}

	@Override
	public String actionName() {
		return actionName;
	}
}
