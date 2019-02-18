package com.hiveworkshop.wc3.gui.modeledit.newstuff.builder;

import java.awt.Cursor;
import java.awt.Graphics2D;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.activity.ButtonType;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.mdl.RenderModel;

public interface ManipulatorBuilder {
	Cursor getCursorAt(int x, int y, CoordinateSystem coordinateSystem, SelectionView selectionView);

	Manipulator buildActivityListener(int x, int y, ButtonType clickedButton, CoordinateSystem coordinateSystem,
			SelectionView selectionView);

	void render(Graphics2D graphics, CoordinateSystem coordinateSystem, SelectionView selectionView,
			RenderModel renderModel);

	void renderStatic(Graphics2D graphics, CoordinateSystem coordinateSystem, SelectionView selectionView);
}
