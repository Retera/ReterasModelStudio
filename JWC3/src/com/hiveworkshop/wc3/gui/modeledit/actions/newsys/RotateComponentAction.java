package com.hiveworkshop.wc3.gui.modeledit.actions.newsys;

import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItem;
import com.hiveworkshop.wc3.gui.modeledit.selection.edits.UniqueComponentSpecificRotation;

public class RotateComponentAction implements UndoAction {
	private final List<? extends SelectionItem> selectedItems;
	private final UniqueComponentSpecificRotation rotation;
	private final UniqueComponentSpecificRotation reverseRotation;

	public RotateComponentAction(final List<? extends SelectionItem> selectedItems,
			final UniqueComponentSpecificRotation rotation) {
		this.selectedItems = selectedItems;
		this.rotation = rotation;
		reverseRotation = rotation.reverse();
	}

	@Override
	public void undo() {
		for (final SelectionItem item : selectedItems) {
			item.forEachComponent(reverseRotation);
		}
	}

	@Override
	public void redo() {
		for (final SelectionItem item : selectedItems) {
			item.forEachComponent(rotation);
		}
	}

	@Override
	public String actionName() {
		return "rotate";
	}
}
