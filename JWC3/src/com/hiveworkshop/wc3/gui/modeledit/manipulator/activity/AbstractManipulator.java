package com.hiveworkshop.wc3.gui.modeledit.manipulator.activity;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;

public abstract class AbstractManipulator implements Manipulator {
	protected final Point2D.Double activityStart = new Point2D.Double();

	@Override
	public final void start(final Double mouseStart, final byte dim1, final byte dim2) {
		activityStart.x = mouseStart.x;
		activityStart.y = mouseStart.y;
		onStart(mouseStart, dim1, dim2);
	}

	protected void onStart(final Double mouseStart, final byte dim1, final byte dim2) {
	}

	@Override
	public void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem) {

	}
}
