package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions;

import java.util.ArrayList;
import java.util.Collection;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;

public final class AddSelectionAction<T> implements UndoAction {

	private final Collection<T> selection;
	private final SelectionManager<T> selectionManager;

	public AddSelectionAction(final Collection<T> selection, final SelectionManager<T> selectionManager) {
		this.selection = new ArrayList<>(selection);
		this.selectionManager = selectionManager;
	}

	@Override
	public void undo() {
		selectionManager.removeSelection(selection);

	}

	@Override
	public void redo() {
		selectionManager.addSelection(selection);
	}

	@Override
	public String actionName() {
		return "add selection";
	}
}
