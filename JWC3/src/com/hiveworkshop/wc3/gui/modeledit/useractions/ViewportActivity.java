package com.hiveworkshop.wc3.gui.modeledit.useractions;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelChangeNotifier;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.SelectingEventHandler;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionTypeApplicator;

public interface ViewportActivity extends SelectionListener, ModelChangeListener {
	ViewportActivity reset(final SelectingEventHandler selectionManager, SelectionTypeApplicator selectionListener,
			final CursorManager cursorManager, final CoordinateSystem coordinateSystem, UndoActionListener undoManager,
			ModelChangeNotifier modelChangeNotifier);

	void mousePressed(MouseEvent e);

	void mouseReleased(MouseEvent e);

	void mouseMoved(MouseEvent e);

	void mouseDragged(MouseEvent e);

	void render(Graphics2D g);

	boolean isEditing();
}
