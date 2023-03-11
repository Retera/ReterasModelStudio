package com.hiveworkshop.rms.util;

import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.function.Consumer;

public class CheckSpinner extends JPanel {
	boolean enabled = true;
	JCheckBox checkBox;
	FloatEditorJSpinner spinner;
	JLabel label;
	Consumer<Boolean> onCheckedConsumer;
	Consumer<Float> valueConsumer;

	public CheckSpinner(String checkboxLabel, String spinnerLabel) {
		this(checkboxLabel, true, spinnerLabel, 0.1);
	}
	public CheckSpinner(String checkboxLabel, String spinnerLabel, double stepSize) {
		this(checkboxLabel, true, spinnerLabel, stepSize);
	}

	public CheckSpinner(String checkboxLabel, boolean initialState, String spinnerLabel) {
		this(checkboxLabel, initialState, spinnerLabel, 0.1);
	}
	public CheckSpinner(String checkboxLabel, boolean initialState, String spinnerLabel, double stepSize) {
		this(checkboxLabel, initialState, spinnerLabel, stepSize, false);
	}

	public CheckSpinner(String checkboxLabel, boolean initialState, String spinnerLabel, double stepSize, boolean title) {
		super(new MigLayout("ins 0, ", "", ""));
		if (title) {
			checkBox = new JCheckBox(checkboxLabel);
			checkBox.setSelected(initialState);
			checkBox.addActionListener(e -> setSpinnerState(checkBox.isSelected()));
			label = new JLabel(spinnerLabel);
			spinner = new FloatEditorJSpinner(0f, 0f, 10000.0f, (float) stepSize, null);

			add(checkBox, "wrap");
			add(label, "");
			add(spinner, "wrap");
		} else {

			checkBox = new JCheckBox();
			checkBox.setSelected(initialState);
			checkBox.addActionListener(e -> setSpinnerState(checkBox.isSelected()));
			label = new JLabel(spinnerLabel);
			spinner = new FloatEditorJSpinner(0f, 0f, 10000.0f, (float) stepSize, null);

			setBorder(BorderFactory.createTitledBorder(checkboxLabel));
			add(label, "gapleft 20");
			add(checkBox, "id box, pos (label.x - box.w/3) (label.y + label.h/2)");
			add(spinner, "wrap");
		}
		setSpinnerState(initialState);
	}

	public CheckSpinner setOnCheckedConsumer(Consumer<Boolean> onCheckedConsumer, boolean consumeOnSet){
		this.onCheckedConsumer = onCheckedConsumer;
		if(consumeOnSet){
			onCheckedConsumer.accept(enabled);
		}
		return this;
	}
	public CheckSpinner setFloatConsumer(Consumer<Float> valueConsumer, boolean consumeOnSet){
		this.valueConsumer = valueConsumer;
		if(valueConsumer != null){
			spinner.setFloatEditingStoppedListener(f -> valueConsumer.accept(enabled ? f : null));
			if(consumeOnSet){
				valueConsumer.accept(enabled ? spinner.getFloatValue() : null);
			}
		} else {
			spinner.setFloatEditingStoppedListener(null);
		}
		return this;
	}

	private void setSpinnerState(boolean selected) {
		enabled = selected;
		spinner.setEnabled(selected);
		label.setEnabled(selected);
		if(onCheckedConsumer != null){
			onCheckedConsumer.accept(selected);
		}
		if(valueConsumer != null){
			valueConsumer.accept(enabled ? spinner.getFloatValue() : null);
		}
		repaint();
	}

	public Float getValue() {
		if (enabled) {
			return spinner.getFloatValue();
		}
		return null;
	}
}
