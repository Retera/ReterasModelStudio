package com.hiveworkshop.wc3.gui.modeledit.actions.newsys;

import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItem;
import com.hiveworkshop.wc3.gui.modeledit.selection.edits.UniqueComponentSpecificTranslation;
import com.hiveworkshop.wc3.mdl.Vertex;

public class VertexModifyComponentAction implements UndoAction {
	private final List<? extends SelectionItem> scaledItems;
	private final List<Vertex> itemTranslations;
	private final String name;

	public VertexModifyComponentAction(final List<? extends SelectionItem> scaledItems,
			final List<Vertex> itemTranslations, final String name) {
		this.scaledItems = scaledItems;
		this.itemTranslations = itemTranslations;
		this.name = name;
	}

	@Override
	public void undo() {
		final UniqueComponentSpecificTranslation callback = new UniqueComponentSpecificTranslation();
		for (int i = 0; i < scaledItems.size(); i++) {
			final SelectionItem selectionItem = scaledItems.get(i);
			final Vertex translation = itemTranslations.get(i);
			selectionItem.forEachComponent(
					callback.resetValues(-(float) translation.x, -(float) translation.y, -(float) translation.z));
		}
	}

	@Override
	public void redo() {
		final UniqueComponentSpecificTranslation callback = new UniqueComponentSpecificTranslation();
		for (int i = 0; i < scaledItems.size(); i++) {
			final SelectionItem selectionItem = scaledItems.get(i);
			final Vertex translation = itemTranslations.get(i);
			selectionItem.forEachComponent(
					callback.resetValues((float) translation.x, (float) translation.y, (float) translation.z));
		}
	}

	@Override
	public String actionName() {
		return name;
	}

}
