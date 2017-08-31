package com.hiveworkshop.wc3.gui.modeledit.manipulator;

import java.awt.Rectangle;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
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

	public void selectRegion(final Rectangle region, final CoordinateSystem coordinateSystem) {
		switch (modeButtonGroup.getActiveButtonType()) {
		case ADD:
			modelEditor.addSelectedRegion(region, coordinateSystem);
			break;
		case SELECT:
			modelEditor.setSelectedRegion(region, coordinateSystem);
			break;
		case DESELECT:
			modelEditor.removeSelectedRegion(region, coordinateSystem);
			break;
		}
	}

}
