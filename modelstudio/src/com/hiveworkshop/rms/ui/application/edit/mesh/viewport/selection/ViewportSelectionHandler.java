package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.util.Vec2;

public final class ViewportSelectionHandler {
	private AbstractSelectionManager selectionManager;

	public ViewportSelectionHandler(AbstractSelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setSelectionManager(AbstractSelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public UndoAction selectRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		if (ProgramGlobals.getSelectionMode() == null) {
			return selectionManager.setSelectedRegion(min, max, coordinateSystem);
		}
		return switch (ProgramGlobals.getSelectionMode()) {
			case ADD -> selectionManager.addSelectedRegion(min, max, coordinateSystem);
			case DESELECT -> selectionManager.removeSelectedRegion(min, max, coordinateSystem);
			case SELECT -> selectionManager.setSelectedRegion(min, max, coordinateSystem);
		};
	}

	public boolean selectableUnderCursor(Vec2 point, CoordinateSystem axes) {
		return selectionManager.selectableUnderCursor(point, axes);
	}

}