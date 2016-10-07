package com.hiveworkshop.wc3.gui.modeledit.actions.newsys;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.useractions.SelectionItem;
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
		for (final SelectionItem item : movedItems) {
			item.translate((float) -moveVector.x, (float) -moveVector.y, (float) -moveVector.z);
		}
	}

	@Override
	public void redo() {
		for (final SelectionItem item : movedItems) {
			item.translate((float) moveVector.x, (float) moveVector.y, (float) moveVector.z);
		}
	}

	@Override
	public String actionName() {
		return "move";
	}

}
