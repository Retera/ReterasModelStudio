package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;

import java.awt.*;
import java.awt.event.MouseEvent;

public interface ViewportActivity extends SelectionListener {
	void viewportChanged(CursorManager cursorManager);

	void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem);

	void mouseReleased(MouseEvent e, CoordinateSystem coordinateSystem);

	void mouseMoved(MouseEvent e, CoordinateSystem coordinateSystem);

	void mouseDragged(MouseEvent e, CoordinateSystem coordinateSystem);

	//ugg
	void render(Graphics2D g, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated);

	boolean isEditing();
}
