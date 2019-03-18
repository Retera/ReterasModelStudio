package com.matrixeaterhayate;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

public class HMToolbox extends JPanel {
	public HMToolbox() {
		setLayout(new GridLayout(2, 1));
		final JPanel createPanel = new JPanel();
		createPanel.setBorder(BorderFactory.createTitledBorder("Create"));
		createPanel.setLayout(new GridLayout(3, 2));
		createPanel.add(new JButton(""));

		final JPanel transformPanel = new JPanel();
		transformPanel.setBorder(BorderFactory.createTitledBorder("Transform"));
		transformPanel.setLayout(new GridLayout(3, 2));
	}
}
