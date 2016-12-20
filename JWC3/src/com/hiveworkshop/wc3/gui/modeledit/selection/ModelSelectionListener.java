package com.hiveworkshop.wc3.gui.modeledit.selection;

import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonListener;

public class ModelSelectionListener implements SelectionTypeApplicator {
	private SelectionMode selectionMode;
	private final SelectionManager selectionManager;

	public ModelSelectionListener(final SelectionManager selectionManager,
			final ToolbarButtonGroup<SelectionMode> modeNotififer) {
		this.selectionManager = selectionManager;
		modeNotififer.addToolbarButtonListener(new ToolbarButtonListener<SelectionMode>() {
			@Override
			public void typeChanged(final SelectionMode newType) {
				selectionMode = newType;
			}
		});
	}

	@Override
	public void chooseGroup(final List<SelectionItem> selectionItem) {
		switch (selectionMode) {
		case ADD:
			selectionManager.addSelection(selectionItem);
			break;
		case DESELECT:
			selectionManager.removeSelection(selectionItem);
			break;
		case SELECT:
			selectionManager.setSelection(selectionItem);
			break;
		}
	}

}
