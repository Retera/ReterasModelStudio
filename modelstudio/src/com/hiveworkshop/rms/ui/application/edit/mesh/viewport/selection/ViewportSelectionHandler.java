package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec2;

public abstract class ViewportSelectionHandler {

	public abstract UndoAction selectRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem);

	public abstract boolean canSelectAt(Vec2 point, CoordinateSystem axes);
}
