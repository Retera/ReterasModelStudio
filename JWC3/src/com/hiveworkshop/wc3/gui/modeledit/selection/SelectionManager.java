package com.hiveworkshop.wc3.gui.modeledit.selection;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;

public interface SelectionManager extends SelectionManagerView {
	@Override
	List<SelectionItem> getSelection();

	/**
	 * @return
	 * @deprecated apply mutations through mutable selection component
	 */
	@Deprecated
	List<Triangle> getSelectedFaces();

	/**
	 * @return
	 * @deprecated apply mutations through mutable selection component
	 */
	@Deprecated()
	List<Vertex> getSelectedVertices();

	List<? extends SelectionItem> getSelectableItems();

	void setSelection(List<? extends SelectionItem> selectionItem);

	void addSelection(List<? extends SelectionItem> selectionItem);

	void removeSelection(List<? extends SelectionItem> selectionItem);

	void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem);

	void addSelectionListener(SelectionListener listener);

	final class Util {
		public static void invertSelection(final SelectionManager selectionManager) {
			final List<SelectionItem> selection = selectionManager.getSelection();
			final Set<SelectionItem> selectionSet = new HashSet<>(selection);
			final List<SelectionItem> unselected = new ArrayList<>();
			for (final SelectionItem item : selectionManager.getSelectableItems()) {
				if (!selectionSet.contains(item)) {
					unselected.add(item);
				}
			}
			selectionManager.setSelection(unselected);
		}

		public static void selectAll(final SelectionManager selectionManager) {
			selectionManager.addSelection(selectionManager.getSelectableItems());
		}

		private Util() {
		}
	}
}
