package com.hiveworkshop.wc3.gui.modeledit.useractions;

import java.util.List;

public interface SelectionManager extends SelectionManagerView {
	@Override
	List<SelectionItem> getSelection();

	List<? extends SelectionItem> getSelectableItems();

	void setSelection(List<SelectionItem> selectionItem);

	void addSelection(List<SelectionItem> selectionItem);

	void removeSelection(List<SelectionItem> selectionItem);

	void addSelectionListener(SelectionListener listener);

}
