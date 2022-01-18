package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.FaceEffect;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ComponentFaceEffectPanel extends ComponentPanel<FaceEffect> {
	private FaceEffect faceEffect;
	private final JTextField targetField;
	private final JTextField effectField;


	public ComponentFaceEffectPanel(ModelHandler modelHandler) {
		super(modelHandler);
		setLayout(new MigLayout("fill", "[][50%:50%:50%, grow][grow]", "[][][grow]"));
		targetField = new JTextField();
		targetField.addFocusListener(setEffectTarget());
		effectField = new JTextField();
		effectField.addFocusListener(setEffect());

		add(new JLabel("Target: "));
		add(targetField, "sg fields, growx, wrap");
		add(new JLabel("Effect"));
		add(effectField, "sg fields, growx, wrap");


	}

	@Override
	public ComponentPanel<FaceEffect> setSelectedItem(FaceEffect itemToSelect) {
		faceEffect = itemToSelect;
		targetField.setText(faceEffect.getFaceEffectTarget());
		effectField.setText(faceEffect.getFaceEffect());

		revalidate();
		repaint();
		return this;
	}

	private FocusAdapter setEffectTarget() {
		return new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				faceEffect.setFaceEffectTarget(targetField.getText());
			}
		};
	}

	private FocusAdapter setEffect() {
		return new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				faceEffect.setFaceEffect(effectField.getText());
			}
		};
	}
}
