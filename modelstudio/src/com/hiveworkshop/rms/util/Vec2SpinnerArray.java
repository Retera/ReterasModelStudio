package com.hiveworkshop.rms.util;

import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.function.Consumer;

public class Vec2SpinnerArray {
	private final FloatEditorJSpinner[] spinners = new FloatEditorJSpinner[2];
	private final JLabel[] labels = new JLabel[2];
	private String labelWrap = "wrap";
	private String spinnerWrap = "";
	private String labelConst = "";
	private String spinnerConst = "";
	private boolean isEnabled = true;
	private float minValue;
	private float maxValue;
	private float stepSize;
	private float[] floats;

	private Consumer<Vec2> vec3Consumer;

	boolean isUpdating = false;

	public Vec2SpinnerArray() {
		this(new Vec2(), "", "");
	}
	public Vec2SpinnerArray(Vec2 startV) {
		this(startV, "", "");
	}
	public Vec2SpinnerArray(Vec2 startV, float minValue, float stepSize) {
		this(startV, "", "", minValue, stepSize);
	}

	public Vec2SpinnerArray(String l1, String l2) {
		this(new Vec2(), l1, l2);
	}

	public Vec2SpinnerArray(Vec2 startV, String l1, String l2) {
		this(startV, l1, l2, -100000.00f, 0.1f);
	}
	public Vec2SpinnerArray(Vec2 startV, String l1, String l2, float minValue, float stepSize) {
		this(startV, l1, l2, minValue, 100000.00f, stepSize);
	}
	public Vec2SpinnerArray(Vec2 startV, String l1, String l2, float minValue, float maxValue, float stepSize) {
		this.floats = startV.toArray();
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.stepSize = stepSize;

		labels[0] = new JLabel(l1);
		labels[1] = new JLabel(l2);
	}
	private FloatEditorJSpinner getStandardSpinner(float startValue, float minValue, float maxValue, float stepSize) {
		return new FloatEditorJSpinner(startValue, minValue, maxValue, stepSize, null);
	}

	public JPanel spinnerPanel() {
		JPanel spinnerPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		JPanel xPanel = getCoordPanel(0);
		JPanel yPanel = getCoordPanel(1);

		spinnerPanel.add(xPanel, spinnerWrap);
		spinnerPanel.add(yPanel, spinnerWrap);

		return spinnerPanel;
	}

	public JPanel getCoordPanel(int i) {
		JPanel coordPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		coordPanel.add(labels[i], labelConst + ", " + labelWrap);
		coordPanel.add(getSpinner(i), spinnerConst);
		return coordPanel;
	}

	public Vec2 getValue() {
		double vX = ((Number) getSpinner(0).getValue()).doubleValue();
		double vY = ((Number) getSpinner(1).getValue()).doubleValue();
		return new Vec2(vX, vY);
	}

	public Vec3 getVec3Value() {
		double vX = ((Number) getSpinner(0).getValue()).doubleValue();
		double vY = ((Number) getSpinner(1).getValue()).doubleValue();
		return new Vec3(vX, vY, 0);
	}

	public Vec2SpinnerArray setValues(Vec2 newValues) {
		isUpdating = true;
		getSpinner(0).reloadNewValue(newValues.x);
		getSpinner(1).reloadNewValue(newValues.y);
		isUpdating = false;
		return this;
	}

	public Vec2SpinnerArray setEnabled(boolean b) {
		isEnabled = b;
		if(spinners[0] != null) spinners[0].setEnabled(b);
		if(spinners[1] != null) spinners[1].setEnabled(b);
		labels[0].setEnabled(b);
		labels[1].setEnabled(b);
		return this;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public Vec2SpinnerArray setMaxValue(float maxValue) {
		this.maxValue = maxValue;
		return this;
	}

	public Vec2SpinnerArray setMinValue(float minValue) {
		this.minValue = minValue;
		return this;
	}

	public Vec2SpinnerArray setStepSize(float stepSize) {
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

	public Vec2SpinnerArray setLabelWrap(boolean b) {
		labelWrap = b ? "wrap" : "";
		return this;
	}

	public Vec2SpinnerArray setSpinnerWrap(boolean b) {
		spinnerWrap = b ? "wrap" : "";
		return this;
	}

	public Vec2SpinnerArray setLabelConstrains(String constrains) {
		labelConst = constrains;
		return this;
	}

	public Vec2SpinnerArray setSpinnerConstrains(String constrains) {
		spinnerConst = constrains;
		return this;
	}

	public Vec2SpinnerArray setStepSize(double stepSize){
		if(spinners[0] != null) ((SpinnerNumberModel)spinners[0].getModel()).setStepSize(stepSize);
		if(spinners[1] != null) ((SpinnerNumberModel)spinners[1].getModel()).setStepSize(stepSize);
		return this;
	}

	public Vec2SpinnerArray setVec2Consumer(Consumer<Vec2> consumer) {
		this.vec3Consumer = consumer;
		if(spinners[0] != null) spinners[0].setFloatEditingStoppedListener(f -> runConsumer());
		if(spinners[1] != null) spinners[1].setFloatEditingStoppedListener(f -> runConsumer());
		return this;
	}


	private void runConsumer() {
		if (vec3Consumer != null) {
			vec3Consumer.accept(getValue());
		}
	}
}
