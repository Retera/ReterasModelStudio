package com.hiveworkshop.rms.ui.application;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class MouseCoordDisplay {
	private final JTextField[] mouseCoordDisplay;

	public MouseCoordDisplay() {
		mouseCoordDisplay = new JTextField[3];
		for (int i = 0; i < mouseCoordDisplay.length; i++) {
			mouseCoordDisplay[i] = new JTextField("");
			mouseCoordDisplay[i].setMaximumSize(new Dimension(80, 18));
			mouseCoordDisplay[i].setMinimumSize(new Dimension(50, 15));
			mouseCoordDisplay[i].setEditable(false);
		}
	}
	public void setMouseCoordDisplay(double x, double y) {
		mouseCoordDisplay[0].setText(String.format(Locale.US, "%3.4f", x));
		mouseCoordDisplay[1].setText(String.format(Locale.US, "%3.4f", y));
	}
}
