package com.hiveworkshop.wc3.gui.modeledit.selection;

import com.hiveworkshop.wc3.util.Callback;

public final class SelectionUtils {
	public static void applyToSelection(final SelectionManager selectionManager,
			final Callback<MutableSelectionComponent> callback) {
		for (final SelectionItem item : selectionManager.getSelection()) {
			item.forEachComponent(callback);
		}
	}

	private SelectionUtils() {
	}
}
