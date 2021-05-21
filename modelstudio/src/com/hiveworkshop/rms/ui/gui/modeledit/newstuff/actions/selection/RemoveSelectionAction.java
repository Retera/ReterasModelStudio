package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;

import java.util.ArrayList;
import java.util.Collection;

public final class RemoveSelectionAction<T> implements UndoAction {

	private final Collection<T> selection;
	private final SelectionManager<T> selectionManager;
	private final Collection<T> previousSelection;

	public RemoveSelectionAction(Collection<T> previousSelection, Collection<T> selection, SelectionManager<T> selectionManager) {
		this.previousSelection = new ArrayList<>(previousSelection);
		this.selection = new ArrayList<>(selection);
		this.selectionManager = selectionManager;
	}

	@Override
	public void undo() {
		selectionManager.setSelection(previousSelection);

	}

	@Override
	public void redo() {
		selectionManager.removeSelection(selection);
	}

	@Override
	public String actionName() {
		return "deselect";
	}
}
