package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.ModelChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;

public interface ViewportActivity extends SelectionListener, ModelChangeListener {
	void viewportChanged(CursorManager cursorManager);

	void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem);

	void mouseReleased(MouseEvent e, CoordinateSystem coordinateSystem);

	void mouseMoved(MouseEvent e, CoordinateSystem coordinateSystem);

	void mouseDragged(MouseEvent e, CoordinateSystem coordinateSystem);

	void render(Graphics2D g, CoordinateSystem coordinateSystem, RenderModel renderModel);

	void renderStatic(Graphics2D g, CoordinateSystem coordinateSystem);

	boolean isEditing();
}
