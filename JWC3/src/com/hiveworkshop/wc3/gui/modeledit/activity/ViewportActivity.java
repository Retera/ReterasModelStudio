package com.hiveworkshop.wc3.gui.modeledit.activity;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.listener.ModelEditorChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionListener;

public interface ViewportActivity extends SelectionListener, ModelChangeListener, ModelEditorChangeListener {
	void viewportChanged(CursorManager cursorManager, CoordinateSystem coordinateSystem);

	void mousePressed(MouseEvent e);

	void mouseReleased(MouseEvent e);

	void mouseMoved(MouseEvent e);

	void mouseDragged(MouseEvent e);

	void render(Graphics2D g);

	boolean isEditing();
}
