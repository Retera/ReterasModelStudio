package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class Manipulator {
	protected final Vec2 activityStart = new Vec2();

	public final void start(MouseEvent e, Vec2 mouseStart, byte dim1, byte dim2) {
		activityStart.set(mouseStart);
		onStart(e, mouseStart, dim1, dim2);
	}

	protected void onStart(MouseEvent e, Vec2 mouseStart, byte dim1, byte dim2) {
	}

	public abstract void update(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2);

	public abstract UndoAction finish(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2);

	public void render(Graphics2D graphics, CoordinateSystem coordinateSystem) {

	}
}
