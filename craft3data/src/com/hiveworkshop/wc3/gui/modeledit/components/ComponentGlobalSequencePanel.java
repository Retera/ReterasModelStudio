package com.hiveworkshop.wc3.gui.modeledit.components;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;

public class ComponentGlobalSequencePanel extends JPanel {
	private final JLabel indexLabel;
	private final JSpinner lengthSpinner;

	public ComponentGlobalSequencePanel() {
		setLayout(new MigLayout());
		lengthSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		add(new JLabel("GlobalSequence "), "cell 0 0");
		indexLabel = new JLabel();
		add(indexLabel, "cell 1 0");
		add(new JLabel("Duration: "), "cell 0 1");
		add(lengthSpinner, "cell 1 1");
	}

	public void setGlobalSequence(final Integer value, final int globalSequenceId) {
		indexLabel.setText(Integer.toString(globalSequenceId));
		lengthSpinner.setValue(value);
	}
}
