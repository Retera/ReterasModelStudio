package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.editor.actions.editor.AbstractShrinkFattenAction;
import com.hiveworkshop.rms.editor.actions.editor.CameraShrinkFattenAction;
import com.hiveworkshop.rms.editor.actions.editor.CompoundShrinkFattenAction;
import com.hiveworkshop.rms.editor.actions.editor.StaticMeshShrinkFattenAction;
import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.TwiNumberSlider;
import com.hiveworkshop.rms.util.uiFactories.CheckBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.util.ArrayList;
import java.util.List;

public class ShrinkFattenPanel extends JPanel {
	private CompoundShrinkFattenAction shrinkFattenCompoundAction;
	private final FloatEditorJSpinner shrinkFattenSpinner;
	private final TwiNumberSlider slider;
	private float lastValue = 0;
	private ModelHandler modelHandler;
	private boolean keepSharp;

	public ShrinkFattenPanel() {
		super(new MigLayout("gap 0"));
		add(new JLabel("Shrink/Fatten"), "spanx, wrap");

		slider = new TwiNumberSlider(-1200, 1200, 0, true, true);
		slider.setMinLowerLimit(-100000).setMaxUpperLimit(100000);
		slider.addChangeListener(e -> onShrinkFatten(e, slider));
		add(slider, "spanx, wrap");

		shrinkFattenSpinner = new FloatEditorJSpinner(0,-1000, 1000, 0.1f, null);
		add(shrinkFattenSpinner);

		JButton button = new JButton("Shrink/Fatten");
		button.addActionListener(e -> onShrinkFatten(shrinkFattenSpinner.getFloatValue()));
		add(button, "wrap");
		add(CheckBox.create("Keep sharp edges together", b -> keepSharp = b), "spanx,wrap");
	}

	public ShrinkFattenPanel setModel(ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		return this;
	}

	private void onShrinkFatten(ChangeEvent e, JSlider slider) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			if (slider.getValueIsAdjusting()) {

				float newValue = getValue(slider.getValue());
				float value = newValue - lastValue;

				if (shrinkFattenCompoundAction == null) {
					List<AbstractShrinkFattenAction> actions = new ArrayList<>(2);
					if (!modelHandler.getModelView().getSelectedVertices().isEmpty()) {
						actions.add(new StaticMeshShrinkFattenAction(modelHandler.getModelView().getSelectedVertices(), value, !keepSharp));
					}
					if (!modelHandler.getModelView().getSelectedCameraNodes().isEmpty()) {
						actions.add(new CameraShrinkFattenAction(modelHandler.getModelView().getSelectedCameraNodes(), value, !keepSharp));
					}

					shrinkFattenCompoundAction = new CompoundShrinkFattenAction(value, actions);
				} else {
					shrinkFattenCompoundAction.updateAmount(value);
				}

				lastValue = newValue;
				shrinkFattenSpinner.reloadNewValue(lastValue);
			} else if (shrinkFattenCompoundAction != null) {
				modelHandler.getUndoManager().pushAction(shrinkFattenCompoundAction);
				shrinkFattenCompoundAction = null;
				lastValue = 0;
				slider.setValue(0);
			}
		}
	}

	private void onShrinkFatten(float amount) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			List<AbstractShrinkFattenAction> actions = new ArrayList<>(2);
			if (!modelHandler.getModelView().getSelectedVertices().isEmpty()) {
				actions.add(new StaticMeshShrinkFattenAction(modelHandler.getModelView().getSelectedVertices(), amount, !keepSharp));
			}
			if (!modelHandler.getModelView().getSelectedCameraNodes().isEmpty()) {
				actions.add(new CameraShrinkFattenAction(modelHandler.getModelView().getSelectedCameraNodes(), amount, !keepSharp));
			}

			if (amount != 0f && !actions.isEmpty()) {
				CompoundShrinkFattenAction shrinkFattenCompoundAction = new CompoundShrinkFattenAction(amount, actions);
				modelHandler.getUndoManager().pushAction(shrinkFattenCompoundAction.redo());
			}

		}
	}


	private float getValue(int i) {
		float a = .2f;
		float b = 0.1f;
		float c = 0.01f;
		return getValue(i, a, b, c);
	}
	private static float getValue(int i, float a, float b, float c) {
		float x = Math.abs(i/100f);
		float x2 = x*x;
		float x3 = x*x*x;
		return Math.copySign(x*a + x2*b + x3*c, i);
	}
}
