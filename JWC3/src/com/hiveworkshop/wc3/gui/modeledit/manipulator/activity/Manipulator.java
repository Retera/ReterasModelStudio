package com.hiveworkshop.wc3.gui.modeledit.manipulator.activity;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;

public interface Manipulator {
	void start(Point2D.Double mouseStart, byte dim1, byte dim2);

	void update(Point2D.Double mouseStart, Point2D.Double mouseEnd, byte dim1, byte dim2);

	UndoAction finish(Point2D.Double mouseStart, Point2D.Double mouseEnd, byte dim1, byte dim2);

	void render(Graphics2D graphics, CoordinateSystem coordinateSystem);
}
