package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.selection.ViewportSelectionHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

public class SelectManipulator extends Manipulator {
	private final ViewportSelectionHandler eventHandler;
	private Vec2 mouseEnd;
	private final ProgramPreferences programPreferences;
	private final CoordinateSystem coordinateSystem;
	private byte currentDim1;
	private byte currentDim2;

	public SelectManipulator(ViewportSelectionHandler eventHandler, ProgramPreferences programPreferences, CoordinateSystem coordinateSystem) {
		this.eventHandler = eventHandler;
		this.programPreferences = programPreferences;
		this.coordinateSystem = coordinateSystem;
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, byte dim1, byte dim2) {
		currentDim1 = dim1;
		currentDim2 = dim2;
	}

	@Override
	public void update(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		this.mouseEnd = mouseEnd;
	}

	@Override
	public UndoAction finish(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		Vec2 min = new Vec2(activityStart).minimize(mouseEnd);
		Vec2 max = new Vec2(activityStart).maximize(mouseEnd);
		return eventHandler.selectRegion(new Rectangle2D.Double(min.x, min.y, max.x - min.x, max.y - min.y), coordinateSystem);
	}

	@Override
	public void render(Graphics2D graphics, CoordinateSystem coordinateSystem) {
		if ((activityStart == null) || (mouseEnd == null)) {
			return;
		}
		if ((currentDim1 == coordinateSystem.getPortFirstXYZ()) && (currentDim2 == coordinateSystem.getPortSecondXYZ())) {
			double minX = Math.min(coordinateSystem.viewX(activityStart.x), coordinateSystem.viewX(mouseEnd.x));
			double minY = Math.min(coordinateSystem.viewY(activityStart.y), coordinateSystem.viewY(mouseEnd.y));
			double maxX = Math.max(coordinateSystem.viewX(activityStart.x), coordinateSystem.viewX(mouseEnd.x));
			double maxY = Math.max(coordinateSystem.viewY(activityStart.y), coordinateSystem.viewY(mouseEnd.y));
			graphics.setColor(programPreferences.getSelectColor());
			graphics.drawRect((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY));
		}
	}
}
