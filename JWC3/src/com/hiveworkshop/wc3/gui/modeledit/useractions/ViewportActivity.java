package com.hiveworkshop.wc3.gui.modeledit.useractions;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;

public interface ViewportActivity {
	ViewportActivity reset(final SelectionManager selectionManager, SelectionListener selectionListener,
			final CursorManager cursorManager, final CoordinateSystem coordinateSystem, Runnable updateListener);

	void mousePressed(MouseEvent e);

	void mouseReleased(MouseEvent e);

	void mouseMoved(MouseEvent e);

	void mouseDragged(MouseEvent e);

	void render(Graphics2D g);
}
