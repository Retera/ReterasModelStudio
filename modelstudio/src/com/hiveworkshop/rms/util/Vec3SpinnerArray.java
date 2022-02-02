package com.hiveworkshop.rms.util;

import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.function.Consumer;

public class Vec3SpinnerArray {
	private FloatEditorJSpinner[] spinners = new FloatEditorJSpinner[3];
	private JLabel[] labels = new JLabel[3];
	private String labelWrap = "wrap";
	private String spinnerWrap = "";
	private String labelConst = "";
	private String spinnerConst = "";
	private boolean isEnabled = true;

	private Consumer<Vec3> vec3Consumer;

	boolean isUpdating = false;

	public Vec3SpinnerArray() {
		this(new Vec3(), "", "", "");
	}

	public Vec3SpinnerArray(String l1, String l2, String l3) {
		this(new Vec3(), l1, l2, l3);
	}

	public Vec3SpinnerArray(Vec3 startV, String l1, String l2, String l3) {
		spinners[0] = getStandardSpinner(startV.x).reloadNewValue(startV.x);
		spinners[1] = getStandardSpinner(startV.y).reloadNewValue(startV.y);
		spinners[2] = getStandardSpinner(startV.z).reloadNewValue(startV.z);

		labels[0] = new JLabel(l1);
		labels[1] = new JLabel(l2);
		labels[2] = new JLabel(l3);
	}

	private FloatEditorJSpinner getStandardSpinner(double startValue) {
		return new FloatEditorJSpinner((float) startValue, -100000.00f, .1f);
	}

	public JPanel spinnerPanel() {
		JPanel spinnerPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		JPanel xPanel = getCoordPanel(0);
		JPanel yPanel = getCoordPanel(1);
		JPanel zPanel = getCoordPanel(2);

		spinnerPanel.add(xPanel, spinnerWrap);
		spinnerPanel.add(yPanel, spinnerWrap);
		spinnerPanel.add(zPanel, spinnerWrap);

		return spinnerPanel;
	}

	public JPanel getCoordPanel(int i) {
		JPanel coordPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		coordPanel.add(labels[i], labelConst + ", " + labelWrap);
		coordPanel.add(spinners[i], spinnerConst);
		return coordPanel;
	}

	public Vec3 getValue() {
		double vX = ((Number) spinners[0].getValue()).doubleValue();
		double vY = ((Number) spinners[1].getValue()).doubleValue();
		double vZ = ((Number) spinners[2].getValue()).doubleValue();
		return new Vec3(vX, vY, vZ);
	}

	public Vec3SpinnerArray setValues(Vec3 newValues) {
		isUpdating = true;
		spinners[0].reloadNewValue(newValues.x);
		spinners[1].reloadNewValue(newValues.y);
		spinners[2].reloadNewValue(newValues.z);
		isUpdating = false;
		return this;
	}

	public Vec3SpinnerArray setEnabled(boolean b) {
		isEnabled = b;
		spinners[0].setEnabled(b);
		spinners[1].setEnabled(b);
		spinners[2].setEnabled(b);
		return this;
	}

	public boolean isEnabled() {
		return isEnabled;
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

	public Vec3SpinnerArray setStepSize(double stepSize){
		((SpinnerNumberModel)spinners[0].getModel()).setStepSize(stepSize);
		((SpinnerNumberModel)spinners[1].getModel()).setStepSize(stepSize);
		((SpinnerNumberModel)spinners[2].getModel()).setStepSize(stepSize);
		return this;
	}

	public Vec3SpinnerArray setVec3Consumer(Consumer<Vec3> consumer) {
		this.vec3Consumer = consumer;
		spinners[0].setFloatEditingStoppedListener(f -> runConsumer());
		spinners[1].setFloatEditingStoppedListener(f -> runConsumer());
		spinners[2].setFloatEditingStoppedListener(f -> runConsumer());
		return this;
	}


	private void runConsumer() {
		if (vec3Consumer != null) {
			vec3Consumer.accept(getValue());
		}
	}
}
