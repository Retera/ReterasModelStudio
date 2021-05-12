package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;
import com.hiveworkshop.rms.util.Vec2;

public final class ViewportSelectionHandlerImpl implements ViewportSelectionHandler {
	private final ToolbarButtonGroup2<SelectionMode> modeButtonGroup;
	private ModelEditor selectingEventHandler;

	public ViewportSelectionHandlerImpl(ToolbarButtonGroup2<SelectionMode> modeButtonGroup,
	                                    ModelEditor selectingEventHandler) {
		this.modeButtonGroup = modeButtonGroup;
		this.selectingEventHandler = selectingEventHandler;
	}

	public void setSelectingEventHandler(ModelEditor selectingEventHandler) {
		this.selectingEventHandler = selectingEventHandler;
	}

	@Override
	public UndoAction selectRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		if (modeButtonGroup.getActiveButtonType() == null){
			return selectingEventHandler.setSelectedRegion(min, max, coordinateSystem);
		}
		return switch (modeButtonGroup.getActiveButtonType()) {
			case ADD -> selectingEventHandler.addSelectedRegion(min, max, coordinateSystem);
			case DESELECT -> selectingEventHandler.removeSelectedRegion(min, max, coordinateSystem);
			case SELECT -> selectingEventHandler.setSelectedRegion(min, max, coordinateSystem);
		};
	}

	@Override
	public boolean canSelectAt(final Vec2 point, final CoordinateSystem axes) {
		return selectingEventHandler.canSelectAt(point, axes);
	}

}