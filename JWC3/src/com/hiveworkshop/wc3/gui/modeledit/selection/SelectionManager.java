package com.hiveworkshop.wc3.gui.modeledit.selection;

import java.awt.Graphics2D;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;

public interface SelectionManager {
	List<? extends SelectionItemView> getSelection();

	List<? extends SelectionItem> getSelectableItems();

	void setSelection(List<SelectionItem> selectionItem);

	void addSelection(List<SelectionItem> selectionItem);

	void removeSelection(List<SelectionItem> selectionItem);

	void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem);
}
