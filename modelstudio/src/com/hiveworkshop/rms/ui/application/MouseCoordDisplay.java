package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;

import javax.swing.*;
import java.awt.*;

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

	public MouseCoordDisplay setMouseCoord(CoordinateSystem coordinateSystem, double value1, double value2) {
		for (final JTextField jTextField : mouseCoordDisplay) {
			jTextField.setText("");
		}
		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();
		if (coordinateSystem.getPortFirstXYZ() < 0) {
			dim1 = (byte) (-dim1 - 1);
			value1 = -value1;
		}
		if (dim2 < 0) {
			dim2 = (byte) (-dim2 - 1);
			value2 = -value2;
		}
		mouseCoordDisplay[dim1].setText((float) value1 + "");
		mouseCoordDisplay[dim2].setText((float) value2 + "");
		return this;
	}
}
