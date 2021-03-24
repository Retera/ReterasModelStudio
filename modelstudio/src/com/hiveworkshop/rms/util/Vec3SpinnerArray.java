package com.hiveworkshop.rms.util;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class Vec3SpinnerArray {
	JSpinner[] spinners = new JSpinner[3];
	JLabel[] labels = new JLabel[3];
	String labelWrap = "wrap";
	String spinnerWrap = "";
	String labelConst = "";
	String spinnerConst = "";


	public Vec3SpinnerArray() {
		spinners[0] = getStandardSpinner(0);
		spinners[1] = getStandardSpinner(0);
		spinners[2] = getStandardSpinner(0);
		labels[0] = new JLabel("");
		labels[1] = new JLabel("");
		labels[2] = new JLabel("");
	}

	public Vec3SpinnerArray(Vec3 startV, String l1, String l2, String l3) {
		spinners[0] = getStandardSpinner(startV.x);
		spinners[1] = getStandardSpinner(startV.y);
		spinners[2] = getStandardSpinner(startV.z);
		labels[0] = new JLabel(l1);
		labels[1] = new JLabel(l2);
		labels[2] = new JLabel(l3);
	}

	public Vec3SpinnerArray(String l1, String l2, String l3) {
		spinners[0] = getStandardSpinner(0);
		spinners[1] = getStandardSpinner(0);
		spinners[2] = getStandardSpinner(0);
		labels[0] = new JLabel(l1);
		labels[1] = new JLabel(l2);
		labels[2] = new JLabel(l3);
	}

	static JSpinner getStandardSpinner(double startValue) {
		return new JSpinner(new SpinnerNumberModel(startValue, -100000.00, 100000.00, 0.1));
	}

	public JPanel spinnerPanel() {
		JPanel spinnerPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		JPanel xPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		xPanel.add(labels[0], labelConst + ", " + labelWrap);
		xPanel.add(spinners[0], spinnerConst);
		JPanel yPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		yPanel.add(labels[1], labelConst + ", " + labelWrap);
		yPanel.add(spinners[1], spinnerConst);
		JPanel zPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		zPanel.add(labels[2], labelConst + ", " + labelWrap);
		zPanel.add(spinners[2], spinnerConst);

		spinnerPanel.add(xPanel, spinnerWrap);
		spinnerPanel.add(yPanel, spinnerWrap);
		spinnerPanel.add(zPanel, spinnerWrap);

		return spinnerPanel;
	}

	public Vec3 getValue() {
		double vX = ((Number) spinners[0].getValue()).doubleValue();
		double vY = ((Number) spinners[1].getValue()).doubleValue();
		double vZ = ((Number) spinners[2].getValue()).doubleValue();
		return new Vec3(vX, vY, vZ);
	}

	public Vec3SpinnerArray setValues(Vec3 newValues) {
		spinners[0].setValue(newValues.x);
		spinners[1].setValue(newValues.y);
		spinners[2].setValue(newValues.z);
		return this;
	}

	public Vec3SpinnerArray setEnabled(boolean b) {
		spinners[0].setEnabled(b);
		spinners[1].setEnabled(b);
		spinners[2].setEnabled(b);
		return this;
	}

	public Vec3SpinnerArray setLabelWrap(boolean b) {
		labelWrap = b ? "wrap" : "";
		return this;
	}

	public Vec3SpinnerArray setSpinnerWrap(boolean b) {
		spinnerWrap = b ? "wrap" : "";
		return this;
	}

	public Vec3SpinnerArray setLabelConstrains(String constrains) {
		labelConst = constrains;
		return this;
	}

	public Vec3SpinnerArray setSpinnerConstrains(String constrains) {
		spinnerConst = constrains;
		return this;
	}
}
