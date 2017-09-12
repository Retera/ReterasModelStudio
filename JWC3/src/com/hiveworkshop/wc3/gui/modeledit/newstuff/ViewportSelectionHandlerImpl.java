package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.awt.Point;
import java.awt.geom.Rectangle2D;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;

public final class ViewportSelectionHandlerImpl implements ViewportSelectionHandler {
	private final ToolbarButtonGroup<SelectionMode> modeButtonGroup;
	private SelectingEventHandler selectingEventHandler;

	public ViewportSelectionHandlerImpl(final ToolbarButtonGroup<SelectionMode> modeButtonGroup,
			final SelectingEventHandler selectingEventHandler) {
		this.modeButtonGroup = modeButtonGroup;
		this.selectingEventHandler = selectingEventHandler;
	}

	public void setSelectingEventHandler(final SelectingEventHandler selectingEventHandler) {
		this.selectingEventHandler = selectingEventHandler;
	}

	@Override
	public UndoAction selectRegion(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
		switch (modeButtonGroup.getActiveButtonType()) {
		case ADD:
			return selectingEventHandler.addSelectedRegion(region, coordinateSystem);
		case DESELECT:
			return selectingEventHandler.removeSelectedRegion(region, coordinateSystem);
		default:
		case SELECT:
			return selectingEventHandler.setSelectedRegion(region, coordinateSystem);
		}
	}

	@Override
	public boolean canSelectAt(final Point point, final CoordinateSystem axes) {
		return selectingEventHandler.canSelectAt(point, axes);
	}

}