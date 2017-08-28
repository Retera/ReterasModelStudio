package com.hiveworkshop.wc3.gui.modeledit.manipulator.activity;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

public abstract class AbstractBetterActivityListener implements BetterActivityListener {
	protected final Point2D.Double activityStart = new Point2D.Double();

	@Override
	public final void start(final Double mouseStart, final byte dim1, final byte dim2) {
		activityStart.x = mouseStart.x;
		activityStart.y = mouseStart.y;
		onStart(mouseStart, dim1, dim2);
	}

	protected void onStart(final Double mouseStart, final byte dim1, final byte dim2) {
	}
}
