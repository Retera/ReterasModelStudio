package com.hiveworkshop.rms.util;

import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.function.Consumer;

public class Vec1SpinnerArray {
	private FloatEditorJSpinner[] spinners = new FloatEditorJSpinner[1];
	private JLabel[] labels = new JLabel[1];
	private String labelWrap = "wrap";
	private String spinnerWrap = "";
	private String labelConst = "";
	private String spinnerConst = "";
	private boolean isEnabled = true;
	private float minValue;
	private float maxValue;
	private float stepSize;
	private float[] floats;

	private Consumer<Float> vec3Consumer;

	boolean isUpdating = false;

	public Vec1SpinnerArray() {
		this(0.0f, "");
	}
	public Vec1SpinnerArray(Float startV) {
		this(startV, "");
	}
	public Vec1SpinnerArray(Float startV, float minValue, float stepSize) {
		this(startV, "", minValue, stepSize);
	}

	public Vec1SpinnerArray(String l1) {
		this(0.0f, l1);
	}

	public Vec1SpinnerArray(Float startV, String l1) {
		this(startV, l1, -100000.00f, 0.1f);
	}
	public Vec1SpinnerArray(Float startV, String l1, float minValue, float stepSize) {
		this(startV, l1, minValue, 100000.00f, stepSize);
	}
	public Vec1SpinnerArray(Float startV, String l1, float minValue, float maxValue, float stepSize) {
		this.floats = new float[] {startV};
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.stepSize = stepSize;

		labels[0] = new JLabel(l1);
	}

	private FloatEditorJSpinner getStandardSpinner(float startValue, float minValue, float stepSize) {
		return new FloatEditorJSpinner(startValue, minValue, stepSize);
	}
	private FloatEditorJSpinner getStandardSpinner(float startValue, float minValue, float maxValue, float stepSize) {
		return new FloatEditorJSpinner(startValue, minValue, maxValue, stepSize, null);
	}

	public JPanel spinnerPanel() {
		JPanel spinnerPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		JPanel xPanel = getCoordPanel(0);

		spinnerPanel.add(xPanel, spinnerWrap);

		return spinnerPanel;
	}

	public JPanel getCoordPanel(int i) {
		JPanel coordPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		coordPanel.add(labels[i], labelConst + ", " + labelWrap);
		coordPanel.add(getSpinner(i), spinnerConst);
		return coordPanel;
	}

	public Float getValue() {
		return ((Number) spinners[0].getValue()).floatValue();
	}

//	public Vec3 getVec3Value() {
//		double vX = ((Number) spinners[0].getValue()).doubleValue();
//		return new Vec3(vX, vY, 0);
//	}

	public Vec1SpinnerArray setValues(Float newValue) {
		isUpdating = true;
		getSpinner(0).reloadNewValue(newValue);
		isUpdating = false;
		return this;
	}

	public Vec1SpinnerArray setEnabled(boolean b) {
		isEnabled = b;
		if(spinners[0] != null) spinners[0].setEnabled(b);
		labels[0].setEnabled(b);
		return this;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public Vec1SpinnerArray setMaxValue(float maxValue) {
		this.maxValue = maxValue;
		return this;
	}

	public Vec1SpinnerArray setMinValue(float minValue) {
		this.minValue = minValue;
		return this;
	}

	public Vec1SpinnerArray setStepSize(float stepSize) {
		this.stepSize = stepSize;
		return this;
	}

	private FloatEditorJSpinner getSpinner(int i){
		if(spinners[i] == null){
			spinners[i] = getStandardSpinner(floats[i], minValue, maxValue, stepSize).reloadNewValue(floats[i]);
			spinners[i].setEnabled(isEnabled);

			if (vec3Consumer != null) spinners[i].setFloatEditingStoppedListener(f -> runConsumer());
		}
		return spinners[i];
	}

	public Vec1SpinnerArray setLabelWrap(boolean b) {
		labelWrap = b ? "wrap" : "";
		return this;
	}

	public Vec1SpinnerArray setSpinnerWrap(boolean b) {
		spinnerWrap = b ? "wrap" : "";
		return this;
	}

	public Vec1SpinnerArray setLabelConstrains(String constrains) {
		labelConst = constrains;
		return this;
	}

	public Vec1SpinnerArray setSpinnerConstrains(String constrains) {
		spinnerConst = constrains;
		return this;
	}

	public Vec1SpinnerArray setStepSize(double stepSize){
		if(spinners[0] != null) ((SpinnerNumberModel)spinners[0].getModel()).setStepSize(stepSize);
		return this;
	}

	public Vec1SpinnerArray setVec2Consumer(Consumer<Float> consumer) {
		this.vec3Consumer = consumer;
		if(spinners[0] != null) spinners[0].setFloatEditingStoppedListener(f -> runConsumer());
		return this;
	}


	private void runConsumer() {
		if (vec3Consumer != null) {
			vec3Consumer.accept(getValue());
		}
	}
}
