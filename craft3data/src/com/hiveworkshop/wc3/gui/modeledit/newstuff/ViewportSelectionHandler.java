package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.awt.Point;
import java.awt.geom.Rectangle2D;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;

public interface ViewportSelectionHandler {
	UndoAction selectRegion(Rectangle2D region, CoordinateSystem coordinateSystem);

	boolean canSelectAt(Point point, CoordinateSystem axes);
}
