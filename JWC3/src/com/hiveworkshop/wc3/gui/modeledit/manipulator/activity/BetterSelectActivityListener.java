package com.hiveworkshop.wc3.gui.modeledit.manipulator.activity;

import java.awt.geom.Point2D.Double;

import com.hiveworkshop.wc3.gui.modeledit.manipulator.SelectingEventHandler;

public class BetterSelectActivityListener implements BetterActivityListener {
	private final SelectingEventHandler eventHandler;

	public BetterSelectActivityListener(final SelectingEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	@Override
	public void start(final Double mouseStart, final byte dim1, final byte dim2) {

	}

	@Override
	public void update(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void finish(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		eventHandler.addSelectedRegion(region, coordinateSystem);
	}

}
