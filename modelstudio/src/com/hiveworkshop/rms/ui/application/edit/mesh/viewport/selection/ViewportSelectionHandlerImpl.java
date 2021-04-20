package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public final class ViewportSelectionHandlerImpl implements ViewportSelectionHandler {
	private final ToolbarButtonGroup<SelectionMode> modeButtonGroup;
	private ModelEditor selectingEventHandler;

	public ViewportSelectionHandlerImpl(ToolbarButtonGroup<SelectionMode> modeButtonGroup,
	                                    ModelEditor selectingEventHandler) {
		this.modeButtonGroup = modeButtonGroup;
		this.selectingEventHandler = selectingEventHandler;
	}

	public void setSelectingEventHandler(ModelEditor selectingEventHandler) {
		this.selectingEventHandler = selectingEventHandler;
	}

	@Override
	public UndoAction selectRegion(Rectangle2D region, CoordinateSystem coordinateSystem) {
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