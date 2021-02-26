package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.awt.*;
import java.awt.geom.Point2D;

public interface Manipulator {
	void start(Point2D.Double mouseStart, byte dim1, byte dim2);

	void update(Point2D.Double mouseStart, Point2D.Double mouseEnd, byte dim1, byte dim2);

	UndoAction finish(Point2D.Double mouseStart, Point2D.Double mouseEnd, byte dim1, byte dim2);

	void render(Graphics2D graphics, CoordinateSystem coordinateSystem);
}
