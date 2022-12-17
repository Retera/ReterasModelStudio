package com.hiveworkshop.rms.util;

import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.function.Consumer;

public class Vec3SpinnerArray {
	private final FloatEditorJSpinner[] spinners = new FloatEditorJSpinner[3];
	private final JLabel[] labels = new JLabel[3];
	private String labelWrap = "wrap";
	private String spinnerWrap = "";
	private String labelConst = "";
	private String spinnerConst = "";
	private boolean isEnabled = true;
	private float minValue;
	private float maxValue;
	private float stepSize;
	private float[] floats;


	private Consumer<Vec3> vec3Consumer;

	boolean isUpdating = false;

	public Vec3SpinnerArray() {
		this(new Vec3(), "", "", "");
	}
	public Vec3SpinnerArray(Vec3 startV) {
		this(startV, "", "", "");
	}
	public Vec3SpinnerArray(Vec3 startV, float minValue, float stepSize) {
		this(startV, "", "", "", minValue, stepSize);
	}

	public Vec3SpinnerArray(String l1, String l2, String l3) {
		this(new Vec3(), l1, l2, l3);
	}

	public Vec3SpinnerArray(Vec3 startV, String l1, String l2, String l3) {
		this(startV, l1, l2, l3, -100000.00f, 0.1f);
	}
	public Vec3SpinnerArray(Vec3 startV, String l1, String l2, String l3, float minValue, float stepSize) {
		this(startV, l1, l2, l3, minValue, 100000.00f, stepSize);
	}
	public Vec3SpinnerArray(Vec3 startV, String l1, String l2, String l3, float minValue, float maxValue, float stepSize) {
		this.floats = startV.toArray();
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.stepSize = stepSize;

		labels[0] = new JLabel(l1);
		labels[1] = new JLabel(l2);
		labels[2] = new JLabel(l3);
	}
	private FloatEditorJSpinner getStandardSpinner(float startValue, float minValue, float maxValue, float stepSize) {
		return new FloatEditorJSpinner(startValue, minValue, maxValue, stepSize, null);
	}

	public JPanel spinnerPanel() {
		return spinnerPanel(null);
	}

	public JPanel spinnerPanel(String title) {
		JPanel spinnerPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		JPanel xPanel = getCoordPanel(0);
		JPanel yPanel = getCoordPanel(1);
		JPanel zPanel = getCoordPanel(2);

		spinnerPanel.add(xPanel, spinnerWrap);
		spinnerPanel.add(yPanel, spinnerWrap);
		spinnerPanel.add(zPanel, spinnerWrap);

		if(title != null){
			spinnerPanel.setBorder(BorderFactory.createTitledBorder(title));
		}

		return spinnerPanel;
	}

	public JPanel getCoordPanel(int i) {
		JPanel coordPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		coordPanel.add(labels[i], labelConst + ", " + labelWrap);
		coordPanel.add(getSpinner(i), spinnerConst);
		return coordPanel;
	}

	public Vec3 getValue() {
		double vX = ((Number) getSpinner(0).getValue()).doubleValue();
		double vY = ((Number) getSpinner(1).getValue()).doubleValue();
		double vZ = ((Number) getSpinner(2).getValue()).doubleValue();
		return new Vec3(vX, vY, vZ);
	}

	public Vec3SpinnerArray setValues(Vec3 newValues) {
		isUpdating = true;
		getSpinner(0).reloadNewValue(newValues.x);
		getSpinner(1).reloadNewValue(newValues.y);
		getSpinner(2).reloadNewValue(newValues.z);
		isUpdating = false;
		return this;
	}

	public Vec3SpinnerArray setEnabled(boolean b) {
		isEnabled = b;
		if(spinners[0] != null) spinners[0].setEnabled(b);
		if(spinners[1] != null) spinners[1].setEnabled(b);
		if(spinners[2] != null) spinners[2].setEnabled(b);
		labels[0].setEnabled(b);
		labels[1].setEnabled(b);
		labels[2].setEnabled(b);
		return this;
	}

	public Vec3SpinnerArray setMaxValue(float maxValue) {
		this.maxValue = maxValue;
		return this;
	}

	public Vec3SpinnerArray setMinValue(float minValue) {
		this.minValue = minValue;
		return this;
	}

	public Vec3SpinnerArray setStepSize(float stepSize) {
		this.stepSize = stepSize;
		return this;
	}

//	private void remakeSpinners(){
//		Vec3 value = getValue();
//		spinners[0] = getStandardSpinner(value.x, minValue, maxValue, stepSize).reloadNewValue(value.x);
//		spinners[1] = getStandardSpinner(value.y, minValue, maxValue, stepSize).reloadNewValue(value.y);
//		spinners[2] = getStandardSpinner(value.z, minValue, maxValue, stepSize).reloadNewValue(value.z);
//
//		spinners[0].setEnabled(isEnabled);
//		spinners[1].setEnabled(isEnabled);
//		spinners[2].setEnabled(isEnabled);
//		if (vec3Consumer != null) {
//			spinners[0].setFloatEditingStoppedListener(f -> runConsumer());
//			spinners[1].setFloatEditingStoppedListener(f -> runConsumer());
//			spinners[2].setFloatEditingStoppedListener(f -> runConsumer());
//		}
//	}

	private FloatEditorJSpinner getSpinner(int i){
		if(spinners[i] == null){
			spinners[i] = getStandardSpinner(floats[i], minValue, maxValue, stepSize).reloadNewValue(floats[i]);
			spinners[i].setEnabled(isEnabled);

			if (vec3Consumer != null) spinners[i].setFloatEditingStoppedListener(f -> runConsumer());
		}
		return spinners[i];
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
		if(spinners[0] != null) ((SpinnerNumberModel)spinners[0].getModel()).setStepSize(stepSize);
		if(spinners[1] != null) ((SpinnerNumberModel)spinners[1].getModel()).setStepSize(stepSize);
		if(spinners[2] != null) ((SpinnerNumberModel)spinners[2].getModel()).setStepSize(stepSize);
		return this;
	}

	public Vec3SpinnerArray setVec3Consumer(Consumer<Vec3> consumer) {
		this.vec3Consumer = consumer;
		if(spinners[0] != null) spinners[0].setFloatEditingStoppedListener(f -> runConsumer());
		if(spinners[1] != null) spinners[1].setFloatEditingStoppedListener(f -> runConsumer());
		if(spinners[2] != null) spinners[2].setFloatEditingStoppedListener(f -> runConsumer());
		return this;
	}


	private void runConsumer() {
		if (vec3Consumer != null) {
			vec3Consumer.accept(getValue());
		}
	}
}
