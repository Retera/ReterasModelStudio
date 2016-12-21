package com.hiveworkshop.wc3.gui.modeledit.actions.newsys;

import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItem;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;

public final class SelectAddComponentAction implements UndoAction {
	private final SelectionManager manager;
	private final List<SelectionItem> items;

	public SelectAddComponentAction(final SelectionManager manager, final List<SelectionItem> items) {
		this.manager = manager;
		this.items = items;
	}

	@Override
	public void undo() {
		manager.removeSelection(items);
	}

	@Override
	public void redo() {
		manager.addSelection(items);
	}

	@Override
	public String actionName() {
		return "selection: add";
	}

}
