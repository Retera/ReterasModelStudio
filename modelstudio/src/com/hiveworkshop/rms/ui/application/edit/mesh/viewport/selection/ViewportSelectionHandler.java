package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;
import com.hiveworkshop.rms.util.Vec2;

public final class ViewportSelectionHandler {
	private final ToolbarButtonGroup2<SelectionMode> modeButtonGroup;
	private AbstractSelectionManager modelEditor;

	public ViewportSelectionHandler(ToolbarButtonGroup2<SelectionMode> modeButtonGroup, AbstractSelectionManager modelEditor) {
		this.modeButtonGroup = modeButtonGroup;
		this.modelEditor = modelEditor;
	}

	public void setModelEditor(AbstractSelectionManager modelEditor) {
		this.modelEditor = modelEditor;
	}

	public UndoAction selectRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		if (modeButtonGroup.getActiveButtonType() == null) {
			return modelEditor.setSelectedRegion(min, max, coordinateSystem);
		}
		return switch (modeButtonGroup.getActiveButtonType()) {
			case ADD -> modelEditor.addSelectedRegion(min, max, coordinateSystem);
			case DESELECT -> modelEditor.removeSelectedRegion(min, max, coordinateSystem);
			case SELECT -> modelEditor.setSelectedRegion(min, max, coordinateSystem);
		};
	}

	public boolean selectableUnderCursor(Vec2 point, CoordinateSystem axes) {
		return modelEditor.selectableUnderCursor(point, axes);
	}

}