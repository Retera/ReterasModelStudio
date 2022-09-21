package com.hiveworkshop.rms.util;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class CheckSpinner extends JPanel {
	boolean enabled = true;
	JCheckBox checkBox;
	SpinnerNumberModel spinnerModel;
	JSpinner spinner;
	JLabel label;

	public CheckSpinner(String checkboxLabel, String spinnerLabel) {
		this(checkboxLabel, true, spinnerLabel);
	}

	public CheckSpinner(String checkboxLabel, boolean initialState, String spinnerLabel) {
		super(new MigLayout("ins 0", "", ""));
		checkBox = new JCheckBox(checkboxLabel);
		checkBox.setSelected(initialState);
		checkBox.addActionListener(e -> setSpinnerState(checkBox.isSelected()));
		label = new JLabel(spinnerLabel);
		spinnerModel = new SpinnerNumberModel(0.0, 0.0, 10000.0, 0.1);
		spinner = new JSpinner(spinnerModel);

		add(checkBox, "wrap");
		add(label, "");
		add(spinner, "wrap");
	}

	private void setSpinnerState(boolean selected) {
		enabled = selected;
		spinner.setEnabled(selected);
		label.setEnabled(selected);
		repaint();
	}

	public Float getValue() {
		if (enabled) {
			return spinnerModel.getNumber().floatValue();
		}
		return null;
	}
}
