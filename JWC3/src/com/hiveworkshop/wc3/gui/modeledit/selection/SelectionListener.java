package com.hiveworkshop.wc3.gui.modeledit.selection;

import java.util.List;

public interface SelectionListener {
	void onSelectionChanged(List<? extends SelectionItemView> previousSelection,
			List<? extends SelectionItemView> newSelection);
}
