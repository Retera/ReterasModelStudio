package com.hiveworkshop.wc3.gui.modeledit.useractions;

import java.util.List;

public interface SelectionManager {
	List<? extends SelectionItemView> getSelection();

	List<? extends SelectionItemView> getSelectableItems();

	void setSelection(List<SelectionItem> selectionItem);

	void addSelection(List<SelectionItem> selectionItem);

	void removeSelection(List<SelectionItem> selectionItem);

}
