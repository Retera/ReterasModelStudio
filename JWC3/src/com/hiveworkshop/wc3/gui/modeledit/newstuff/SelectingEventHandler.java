package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.awt.Point;
import java.awt.geom.Rectangle2D;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;

public interface SelectingEventHandler {
	UndoAction setSelectedRegion(Rectangle2D region, CoordinateSystem coordinateSystem);

	UndoAction removeSelectedRegion(Rectangle2D region, CoordinateSystem coordinateSystem);

	UndoAction addSelectedRegion(Rectangle2D region, CoordinateSystem coordinateSystem);

	UndoAction expandSelection();

	UndoAction invertSelection();

	UndoAction selectAll();

	boolean canSelectAt(Point point, CoordinateSystem axes);
}
