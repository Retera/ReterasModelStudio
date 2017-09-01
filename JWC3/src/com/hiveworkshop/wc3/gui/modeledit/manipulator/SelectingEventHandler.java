package com.hiveworkshop.wc3.gui.modeledit.manipulator;

import java.awt.geom.Rectangle2D;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;

public interface SelectingEventHandler {
	UndoAction setSelectedRegion(Rectangle2D region, byte dim1, byte dim2);

	UndoAction removeSelectedRegion(Rectangle2D region, byte dim1, byte dim2);

	UndoAction addSelectedRegion(Rectangle2D region, byte dim1, byte dim2);

	UndoAction expandSelection();

	UndoAction invertSelection();

	UndoAction selectAll();
}
