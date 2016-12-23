package com.hiveworkshop.wc3.gui.modeledit.selection;

import com.hiveworkshop.wc3.util.Callback;

public interface SelectionItem extends SelectionItemView {
	void forEachComponent(Callback<MutableSelectionComponent> callback);
}
