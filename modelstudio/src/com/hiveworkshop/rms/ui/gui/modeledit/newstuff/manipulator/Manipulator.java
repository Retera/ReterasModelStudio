package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

public abstract class Manipulator {
	protected final Point2D.Double activityStart = new Point2D.Double();

	public final void start(MouseEvent e, final Double mouseStart, final byte dim1, final byte dim2) {
		activityStart.x = mouseStart.x;
		activityStart.y = mouseStart.y;
		onStart(e, mouseStart, dim1, dim2);
	}

	protected void onStart(MouseEvent e, final Double mouseStart, final byte dim1, final byte dim2) {
	}

	public abstract void update(MouseEvent e, final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2);

	public abstract UndoAction finish(MouseEvent e, final Double mouseStart, final Double mouseEnd, final byte dim1, final byte dim2);

	public void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem) {

	}
}
