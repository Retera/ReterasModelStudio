package com.hiveworkshop.wc3.gui.modeledit.actions.newsys;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItem;
import com.hiveworkshop.wc3.gui.modeledit.selection.edits.UniqueComponentSpecificTranslation;
import com.hiveworkshop.wc3.mdl.Vertex;

public final class MoveComponentAction implements UndoAction {
	private final List<? extends SelectionItem> movedItems;
	private final Vertex moveVector;

	public MoveComponentAction(final List<? extends SelectionItem> items, final Vertex moveVector) {
		this.movedItems = new ArrayList<>(items);
		this.moveVector = moveVector;
	}

	@Override
	public void undo() {
		final UniqueComponentSpecificTranslation callback = new UniqueComponentSpecificTranslation();
		for (final SelectionItem item : movedItems) {
			item.forEachComponent(
					callback.resetValues((float) -moveVector.x, (float) -moveVector.y, (float) -moveVector.z));
		}
	}

	@Override
	public void redo() {
		final UniqueComponentSpecificTranslation callback = new UniqueComponentSpecificTranslation();
		for (final SelectionItem item : movedItems) {
			item.forEachComponent(
					callback.resetValues((float) moveVector.x, (float) moveVector.y, (float) moveVector.z));
		}
	}

	@Override
	public String actionName() {
		return "move";
	}

}
