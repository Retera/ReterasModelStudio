package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public interface ViewportSelectionHandler {
	UndoAction selectRegion(Rectangle2D region, CoordinateSystem coordinateSystem);

	boolean canSelectAt(Point point, CoordinateSystem axes);
}
