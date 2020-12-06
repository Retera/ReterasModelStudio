package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection;

import java.awt.Point;
import java.awt.geom.Rectangle2D;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;

public final class ViewportSelectionHandlerImpl implements ViewportSelectionHandler {
	private final ToolbarButtonGroup<SelectionMode> modeButtonGroup;
	private ModelEditor selectingEventHandler;

	public ViewportSelectionHandlerImpl(final ToolbarButtonGroup<SelectionMode> modeButtonGroup,
			final ModelEditor selectingEventHandler) {
		this.modeButtonGroup = modeButtonGroup;
		this.selectingEventHandler = selectingEventHandler;
	}

	public void setSelectingEventHandler(final ModelEditor selectingEventHandler) {
		this.selectingEventHandler = selectingEventHandler;
	}

	@Override
	public UndoAction selectRegion(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
		return switch (modeButtonGroup.getActiveButtonType()) {
			case ADD -> selectingEventHandler.addSelectedRegion(region, coordinateSystem);
			case DESELECT -> selectingEventHandler.removeSelectedRegion(region, coordinateSystem);
			case SELECT -> selectingEventHandler.setSelectedRegion(region, coordinateSystem);
		};
	}

	@Override
	public boolean canSelectAt(final Point point, final CoordinateSystem axes) {
		return selectingEventHandler.canSelectAt(point, axes);
	}

}