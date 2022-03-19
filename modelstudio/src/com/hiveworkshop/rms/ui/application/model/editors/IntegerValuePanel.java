package com.hiveworkshop.rms.ui.application.model.editors;


import com.hiveworkshop.rms.editor.actions.util.ConsumerAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.IntAnimFlag;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;

public class IntegerValuePanel extends ValuePanel<Integer> {

	private int selectedRow;
	private IntEditorJSpinner staticSpinner;

	public IntegerValuePanel(ModelHandler modelHandler, String title) {
		super(modelHandler, title);

		staticSpinner = new IntEditorJSpinner(1, Integer.MIN_VALUE, this::setStaticValue);
//		keyframePanel.getFloatTrackTableModel().addExtraColumn("Track", "", String.class);  // 🎨 \uD83C\uDFA8
	}


	@Override
	IntEditorJSpinner getStaticComponent() {
//		staticSpinner = new ComponentEditorJSpinner(new SpinnerNumberModel(1.0, minValue, maxValue, 0.01));
		staticSpinner = new IntEditorJSpinner(1, Integer.MIN_VALUE, this::setStaticValue);

		((JSpinner.NumberEditor) staticSpinner.getEditor()).getFormat().setMinimumFractionDigits(2);

		JSpinner standinGuiSpinner = new JSpinner(new SpinnerNumberModel(1, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
		staticSpinner.setPreferredSize(standinGuiSpinner.getPreferredSize());
		staticSpinner.setMaximumSize(standinGuiSpinner.getMaximumSize());
		staticSpinner.setMinimumSize(standinGuiSpinner.getMinimumSize());

		return staticSpinner;
	}


	void setStaticValue(int newValue) {

		if (valueSettingFunction != null) {
			undoManager.pushAction(new ConsumerAction<>(valueSettingFunction, newValue, staticValue, title).redo());
//			valueSettingFunction.accept(newValue);
			staticSpinner.reloadNewValue(newValue);
		}
	}


	@Override
	void reloadStaticValue(Integer value) {
		staticSpinner.reloadNewValue(value);
	}


	@Override
	Integer getZeroValue() {
//		return new Bitmap("Textures\\White.dds");
		return 0;
	}

	@Override
	Integer parseValue(String valueString) {
		String polishedString = valueString.replaceAll("[\\D]", "");

		return Integer.parseInt(polishedString);
	}

	protected AnimFlag<Integer> getNewAnimFlag() {
		return new IntAnimFlag(flagName);
	}

}
