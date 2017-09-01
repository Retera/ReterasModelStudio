package com.hiveworkshop.wc3.gui.modeledit.useractions;

import java.awt.Cursor;
import java.awt.Graphics2D;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.activity.Manipulator;

public interface ManipulatorBuilder {
	Cursor getCursorAt(int x, int y, CoordinateSystem coordinateSystem);

	Manipulator buildActivityListener(int x, int y, ButtonType clickedButton, CoordinateSystem coordinateSystem);

	void render(Graphics2D graphics, CoordinateSystem coordinateSystem);
}
