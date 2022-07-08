package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.editor.actions.editor.StaticMeshShrinkFattenAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.util.Set;

public class ShrinkFattenPanel extends JPanel {
	private StaticMeshShrinkFattenAction shrinkFattenAction;
	private final FloatEditorJSpinner shrinkFattenSpinner;
	private float lastValue = 0;
	private ModelHandler modelHandler;

	public ShrinkFattenPanel(){
		super(new MigLayout("gap 0"));
		add(new JLabel("Shrink/Fatten"), "spanx, wrap");

		add(getShrinkFattenSlider(), "spanx, wrap");

		shrinkFattenSpinner = new FloatEditorJSpinner(0,-1000, 1000, 0.1f, null);
		add(shrinkFattenSpinner);

		JButton button = new JButton("Shrink/Fatten");
		button.addActionListener(e -> onShrinkFatten(shrinkFattenSpinner.getFloatValue()));
		add(button, "wrap");
	}

	public ShrinkFattenPanel setModel(ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		return this;
	}

	private JSlider getShrinkFattenSlider(){
		JSlider slider = new JSlider();
		slider.setMinimum(-1000);
		slider.setMaximum(1000);
		slider.addChangeListener(e -> onShrinkFatten(e, slider));

		return slider;
	}

	private void onShrinkFatten(ChangeEvent e, JSlider slider){
		if(modelHandler != null && !modelHandler.getModelView().isEmpty()){
			if (slider.getValueIsAdjusting()) {
				float newValue = getValue(slider.getValue());
				float value = newValue - lastValue;
				if(shrinkFattenAction == null) {
					shrinkFattenAction = new StaticMeshShrinkFattenAction(modelHandler.getModelView().getSelectedVertices(), value);
				} else {
					shrinkFattenAction.updateAmount(value);
				}
				lastValue = newValue;
				shrinkFattenSpinner.reloadNewValue(lastValue);
			} else if (shrinkFattenAction != null) {
//				System.out.println("lastValue: " + lastValue + " (" + slider.getValue() + ")  ia ajd: " + slider.getValueIsAdjusting());
				modelHandler.getUndoManager().pushAction(shrinkFattenAction);
				shrinkFattenAction = null;
				lastValue = 0;
				slider.setValue(0);
			}
		}
	}
	private void onShrinkFatten(float amount){
		if(modelHandler != null && !modelHandler.getModelView().isEmpty()){
			Set<GeosetVertex> vertices = modelHandler.getModelView().getSelectedVertices();
			StaticMeshShrinkFattenAction shrinkFattenAction = new StaticMeshShrinkFattenAction(vertices, amount);
			modelHandler.getUndoManager().pushAction(shrinkFattenAction.redo());
		}
	}


//	private Vec3SpinnerArray sfSpinners;
	private float getValue(int i){
		float a = .2f;
		float b = 0.1f;
		float c = 0f;
//		Vec3 xFacts = sfSpinners.getValue();
		return getValue(i, a, b, c);
//		return getValue(i, xFacts.x, xFacts.y, xFacts.z);
	}
	private static float getValue(int i, float a, float b, float c){
		float x = Math.abs(i/100f);
		float x2 = x*x;
		float x3 = x*x*x;
		return Math.copySign(x*a + x2*b + x3*c, i);
	}
}
