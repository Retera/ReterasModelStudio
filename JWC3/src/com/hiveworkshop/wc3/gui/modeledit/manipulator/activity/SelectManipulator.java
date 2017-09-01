package com.hiveworkshop.wc3.gui.modeledit.manipulator.activity;

import java.awt.Graphics2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.ViewportSelectionHandler;

public class SelectManipulator implements Manipulator {
	private final ViewportSelectionHandler eventHandler;
	private Double mouseStart;
	private Double mouseEnd;
	private final ProgramPreferences programPreferences;

	public SelectManipulator(final ViewportSelectionHandler eventHandler, final ProgramPreferences programPreferences) {
		this.eventHandler = eventHandler;
		this.programPreferences = programPreferences;
	}

	@Override
	public void start(final Double mouseStart, final byte dim1, final byte dim2) {
		this.mouseStart = mouseStart;
	}

	@Override
	public void update(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		this.mouseEnd = mouseEnd;
	}

	@Override
	public UndoAction finish(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		final double minX = Math.min(mouseStart.x, mouseEnd.x);
		final double minY = Math.min(mouseStart.y, mouseEnd.y);
		final double maxX = Math.max(mouseStart.x, mouseEnd.x);
		final double maxY = Math.max(mouseStart.y, mouseEnd.y);
		return eventHandler.selectRegion(new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY), dim1, dim2);
	}

	@Override
	public void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem) {
		final double minX = coordinateSystem.convertX(Math.min(mouseStart.x, mouseEnd.x));
		final double minY = coordinateSystem.convertY(Math.min(mouseStart.y, mouseEnd.y));
		final double maxX = coordinateSystem.convertX(Math.max(mouseStart.x, mouseEnd.x));
		final double maxY = coordinateSystem.convertY(Math.max(mouseStart.y, mouseEnd.y));
		graphics.setColor(programPreferences.getSelectColor());
		graphics.drawRect((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY));
	}
}
