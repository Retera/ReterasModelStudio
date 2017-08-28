package com.hiveworkshop.wc3.gui.modeledit.manipulator.actions;

import java.util.Collection;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;

public final class RemoveSelectionAction<T> implements UndoAction {

	private final Collection<T> selection;
	private final SelectionManager<T> selectionManager;

	public RemoveSelectionAction(final Collection<T> selection, final SelectionManager<T> selectionManager) {
		this.selection = selection;
		this.selectionManager = selectionManager;
	}

	@Override
	public void undo() {
		selectionManager.addSelection(selection);

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
