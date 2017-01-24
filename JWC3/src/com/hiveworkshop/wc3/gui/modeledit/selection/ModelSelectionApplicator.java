package com.hiveworkshop.wc3.gui.modeledit.selection;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.DeselectComponentAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.SelectAddComponentAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.SelectComponentAction;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonListener;
import com.hiveworkshop.wc3.gui.modeledit.useractions.UndoManager;

public class ModelSelectionApplicator implements SelectionTypeApplicator {
	private SelectionMode selectionMode;
	private final SelectionManager selectionManager;
	private final UndoManager undoManager;

	public ModelSelectionApplicator(final SelectionManager selectionManager,
			final ToolbarButtonGroup<SelectionMode> modeNotififer, final UndoManager undoManager) {
		this.selectionManager = selectionManager;
		this.undoManager = undoManager;
		modeNotififer.addToolbarButtonListener(new ToolbarButtonListener<SelectionMode>() {
			@Override
			public void typeChanged(final SelectionMode newType) {
				selectionMode = newType;
			}
		});
	}

	@Override
	public void chooseGroup(final List<SelectionItem> selectionItem) {
		final ArrayList<SelectionItem> previousSelection = new ArrayList<>(selectionManager.getSelection());
		switch (selectionMode) {
		case ADD:
			selectionManager.addSelection(selectionItem);
			undoManager.pushAction(new SelectAddComponentAction(selectionManager, selectionItem));
			break;
		case DESELECT:
			selectionManager.removeSelection(selectionItem);
			undoManager.pushAction(new DeselectComponentAction(selectionManager, selectionItem));
			break;
		case SELECT:
			selectionManager.setSelection(selectionItem);
			undoManager.pushAction(new SelectComponentAction(selectionManager, selectionItem, previousSelection));
			break;
		}
	}

}
