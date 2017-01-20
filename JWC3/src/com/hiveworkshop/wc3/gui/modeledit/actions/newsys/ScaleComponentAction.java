package com.hiveworkshop.wc3.gui.modeledit.actions.newsys;

import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItem;
import com.hiveworkshop.wc3.gui.modeledit.selection.edits.UniqueComponentSpecificScaling;

public class ScaleComponentAction implements UndoAction {
	private final List<? extends SelectionItem> scaledItems;
	final float centerX, centerY, centerZ, scaleX, scaleY, scaleZ;

	public ScaleComponentAction(final List<? extends SelectionItem> scaledItems, final float centerX,
			final float centerY, final float centerZ, final float scaleX, final float scaleY, final float scaleZ) {
		this.scaledItems = scaledItems;
		this.centerX = centerX;
		this.centerY = centerY;
		this.centerZ = centerZ;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.scaleZ = scaleZ;
	}

	@Override
	public void undo() {
		final UniqueComponentSpecificScaling callback = new UniqueComponentSpecificScaling().resetValues(centerX,
				centerY, centerZ, 1 / scaleX, 1 / scaleY, 1 / scaleZ);
		for (final SelectionItem item : scaledItems) {
			item.forEachComponent(callback);
		}
	}

	@Override
	public void redo() {
		final UniqueComponentSpecificScaling callback = new UniqueComponentSpecificScaling().resetValues(centerX,
				centerY, centerZ, scaleX, scaleY, scaleZ);
		for (final SelectionItem item : scaledItems) {
			item.forEachComponent(callback);
		}
	}

	@Override
	public String actionName() {
		return "scale";
	}
}
