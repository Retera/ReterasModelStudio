package com.hiveworkshop.wc3.gui.modeledit.selection;

import java.awt.Graphics2D;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.mdl.Triangle;

public interface SelectionManager extends SelectionManagerView {
	@Override
	List<SelectionItem> getSelection();

	List<Triangle> getSelectedFaces();

	List<? extends SelectionItem> getSelectableItems();

	void setSelection(List<SelectionItem> selectionItem);

	void addSelection(List<SelectionItem> selectionItem);

	void removeSelection(List<SelectionItem> selectionItem);

	void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem);

	void addSelectionListener(SelectionListener listener);
}
