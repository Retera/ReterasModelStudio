package com.hiveworkshop.rms.ui.application.edit.uv;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.rms.util.Vec2;

public final class TVertexViewportSelectionHandlerImpl implements ViewportSelectionHandler {
	private final ToolbarButtonGroup<SelectionMode> modeButtonGroup;
	private TVertexEditor selectingEventHandler;

	public TVertexViewportSelectionHandlerImpl(ToolbarButtonGroup<SelectionMode> modeButtonGroup,
	                                           TVertexEditor selectingEventHandler) {
		this.modeButtonGroup = modeButtonGroup;
		this.selectingEventHandler = selectingEventHandler;
	}

	public void setSelectingEventHandler(final TVertexEditor selectingEventHandler) {
		this.selectingEventHandler = selectingEventHandler;
	}

	@Override
	public UndoAction selectRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		return switch (modeButtonGroup.getActiveButtonType()) {
			case ADD -> selectingEventHandler.addSelectedRegion(min, max, coordinateSystem);
			case DESELECT -> selectingEventHandler.removeSelectedRegion(min, max, coordinateSystem);
			case SELECT -> selectingEventHandler.setSelectedRegion(min, max, coordinateSystem);
		};
	}

	@Override
	public boolean canSelectAt(Vec2 point, CoordinateSystem axes) {
		return selectingEventHandler.canSelectAt(point, axes);
	}

}