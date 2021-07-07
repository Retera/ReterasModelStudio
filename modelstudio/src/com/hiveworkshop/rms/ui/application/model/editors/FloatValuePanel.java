package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.actions.util.ConsumerAction;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;

public class FloatValuePanel extends ValuePanel<Float> {

	private ComponentEditorJSpinner staticSpinner;


	public FloatValuePanel(ModelHandler modelHandler, final String title, UndoManager undoManager) {
		this(modelHandler, title, Double.MAX_VALUE, -Double.MAX_VALUE, undoManager);
	}

	public FloatValuePanel(ModelHandler modelHandler, final String title, double maxValue, double minValue, UndoManager undoManager) {
		super(modelHandler, title, maxValue, minValue, undoManager);
	}

	@Override
	ComponentEditorJSpinner getStaticComponent() {
//		staticSpinner = new ComponentEditorJSpinner(new SpinnerNumberModel(1.0, minValue, maxValue, 0.01));
		staticSpinner = new ComponentEditorJSpinner(new SpinnerNumberModel(1.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.01));
		staticSpinner.addEditingStoppedListener(this::setStaticValue);

		((JSpinner.NumberEditor) staticSpinner.getEditor()).getFormat().setMinimumFractionDigits(2);

		final JSpinner standinGuiSpinner = new JSpinner(new SpinnerNumberModel(1.0, -Long.MAX_VALUE, Long.MAX_VALUE, 1.0));
		staticSpinner.setPreferredSize(standinGuiSpinner.getPreferredSize());
		staticSpinner.setMaximumSize(standinGuiSpinner.getMaximumSize());
		staticSpinner.setMinimumSize(standinGuiSpinner.getMinimumSize());

		return staticSpinner;
	}

	@Override
	void reloadStaticValue(Float value) {
		staticSpinner.reloadNewValue(value);
	}

	void setStaticValue() {
		float newValue = staticSpinner.getFloatValue();

		if (valueSettingFunction != null) {
			undoManager.pushAction(new ConsumerAction<>(valueSettingFunction, newValue, staticValue, title).redo());
//			valueSettingFunction.accept(newValue);
			staticSpinner.reloadNewValue(newValue);
		}
	}

	@Override
	Float getZeroValue() {
		return 0.0f;
	}

	@Override
	Float parseValue(String valueString) {
		valueString = valueString.replaceAll("[^-\\.e\\d]", "");
//		if(valueString.matches(".*\\d.*")){
//			System.out.println("1 \".*\\\\d.*\" - " + valueString);
//		}
//		if(valueString.matches("(-?\\d+(\\.\\d+)?(e\\d+)?)|(-?\\d*(\\.\\d+)(e\\d+)?)")){
//			System.out.println("2 \"(-?\\d+(\\.\\d+)?(e\\d+)?)|(-?\\d*(\\.\\d+)(e\\d+)?)\" - " + valueString);
//		}
//		if(valueString.matches("(-?\\d+(\\.\\d+)?(e\\d+)?)")){
//			System.out.println("3 \"(-?\\\\d+(\\.\\d+)?(e\\d+)?)\" - " + valueString);
//		}
//		if(valueString.matches("(-?\\d*(\\.\\d+)(e\\d+)?)")){
//			System.out.println("4 \"(-?\\d*(\\.\\d+)(e\\d+)?)\" - " + valueString);
//		}
//		if(valueString.matches("(-?\\d*\\.?\\d+(e\\d+)?)")){
//			System.out.println("5 \"(-?\\d*\\.?\\d+(e\\d+)?)\" - " + valueString);
//		}
		if (valueString.matches("(-?\\d+\\.+)")) {
//			System.out.println("5 \"(-?\\d+\\.+)\" - " + valueString);
			valueString = valueString.replace(".", "");
		}
		if (valueString.matches("(-?\\d+\\.+\\d+)")) {
//			System.out.println("5 \"(-?\\d+\\.+\\d+)\" - " + valueString);
			valueString = valueString.replaceAll("(\\.+)", ".");
		}
		if (valueString.matches(".*\\d.*") && valueString.matches("(-?\\d*\\.?\\d+(e\\d+)?)")) {
			return Float.parseFloat(valueString);
		}
		return 0.0f;
	}


}
