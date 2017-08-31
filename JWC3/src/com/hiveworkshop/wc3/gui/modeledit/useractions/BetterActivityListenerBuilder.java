package com.hiveworkshop.wc3.gui.modeledit.useractions;

import java.awt.Cursor;

import com.hiveworkshop.wc3.gui.modeledit.manipulator.activity.BetterActivityListener;

public interface BetterActivityListenerBuilder {
	Cursor getCursorAt(int x, int y);

	BetterActivityListener buildActivityListener(int x, int y, ButtonType clickedButton);
}
