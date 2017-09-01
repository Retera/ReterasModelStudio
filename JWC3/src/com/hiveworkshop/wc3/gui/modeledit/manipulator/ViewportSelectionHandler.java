package com.hiveworkshop.wc3.gui.modeledit.manipulator;

import java.awt.geom.Rectangle2D;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;

public final class ViewportSelectionHandler {
	private final ToolbarButtonGroup<SelectionMode> modeButtonGroup;
	private final SelectingEventHandler modelEditor;

	public ViewportSelectionHandler(final ToolbarButtonGroup<SelectionMode> modeButtonGroup,
			final SelectingEventHandler modelEditor) {
		this.modeButtonGroup = modeButtonGroup;
		this.modelEditor = modelEditor;
	}

	public UndoAction selectRegion(final Rectangle2D region, final byte dim1, final byte dim2) {
		switch (modeButtonGroup.getActiveButtonType()) {
		case ADD:
			return modelEditor.addSelectedRegion(region, dim1, dim2);
		case DESELECT:
			return modelEditor.removeSelectedRegion(region, dim1, dim2);
		default:
		case SELECT:
			return modelEditor.setSelectedRegion(region, dim1, dim2);
		}
	}

}
