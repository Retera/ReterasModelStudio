package com.hiveworkshop.wc3.gui.modeledit.manipulator.activity;

import java.awt.geom.Point2D;

public interface BetterActivityListener {
	void start(Point2D.Double mouseStart, byte dim1, byte dim2);

	void update(Point2D.Double mouseStart, Point2D.Double mouseEnd, byte dim1, byte dim2);

	void finish(Point2D.Double mouseStart, Point2D.Double mouseEnd, byte dim1, byte dim2);
}
