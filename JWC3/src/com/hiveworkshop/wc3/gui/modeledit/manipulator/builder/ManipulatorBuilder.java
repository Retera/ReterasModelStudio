package com.hiveworkshop.wc3.gui.modeledit.manipulator.builder;

import java.awt.Cursor;
import java.awt.Graphics2D;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.activity.ButtonType;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.activity.Manipulator;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.listener.ModelEditorChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;

public interface ManipulatorBuilder extends ModelEditorChangeListener {
	Cursor getCursorAt(int x, int y, CoordinateSystem coordinateSystem, SelectionView selectionView);

	Manipulator buildActivityListener(int x, int y, ButtonType clickedButton, CoordinateSystem coordinateSystem,
			SelectionView selectionView);

	void render(Graphics2D graphics, CoordinateSystem coordinateSystem, SelectionView selectionView);
}
