package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.*;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;

public class SelectManipulator extends AbstractManipulator {
	private final ViewportSelectionHandler eventHandler;
	private Double mouseEnd;
	private final ProgramPreferences programPreferences;
	private final CoordinateSystem coordinateSystem;
	private byte currentDim1;
	private byte currentDim2;

	public SelectManipulator(final ViewportSelectionHandler eventHandler, final ProgramPreferences programPreferences, final CoordinateSystem coordinateSystem) {
		this.eventHandler = eventHandler;
		this.programPreferences = programPreferences;
		this.coordinateSystem = coordinateSystem;
	}

	@Override
	protected void onStart(final Double mouseStart, final byte dim1, final byte dim2) {
		currentDim1 = dim1;
		currentDim2 = dim2;
	}

	@Override
	public void update(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		this.mouseEnd = mouseEnd;
	}

	@Override
	public UndoAction finish(final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2) {
		final double minX = Math.min(activityStart.x, mouseEnd.x);
		final double minY = Math.min(activityStart.y, mouseEnd.y);
		final double maxX = Math.max(activityStart.x, mouseEnd.x);
		final double maxY = Math.max(activityStart.y, mouseEnd.y);
		return eventHandler.selectRegion(new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY), coordinateSystem);
	}

	@Override
	public void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem) {
		if ((activityStart == null) || (mouseEnd == null)) {
			return;
		}
		if ((currentDim1 == coordinateSystem.getPortFirstXYZ()) && (currentDim2 == coordinateSystem.getPortSecondXYZ())) {
			final double minX = Math.min(coordinateSystem.convertX(activityStart.x), coordinateSystem.convertX(mouseEnd.x));
			final double minY = Math.min(coordinateSystem.convertY(activityStart.y), coordinateSystem.convertY(mouseEnd.y));
			final double maxX = Math.max(coordinateSystem.convertX(activityStart.x), coordinateSystem.convertX(mouseEnd.x));
			final double maxY = Math.max(coordinateSystem.convertY(activityStart.y), coordinateSystem.convertY(mouseEnd.y));
			graphics.setColor(programPreferences.getSelectColor());
			graphics.drawRect((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY));
		}
	}
}
