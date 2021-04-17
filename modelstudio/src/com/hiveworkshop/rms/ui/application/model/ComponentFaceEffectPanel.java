package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.FaceEffect;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ComponentFaceEffectPanel extends JPanel implements ComponentPanel<FaceEffect> {
	private final ModelViewManager modelViewManager;
	private final UndoActionListener undoActionListener;
	private final ModelStructureChangeListener modelStructureChangeListener;
	FaceEffect faceEffect;

	JTextField targetField;
	JTextField effectField;


	public ComponentFaceEffectPanel(final ModelViewManager modelViewManager,
	                                final UndoActionListener undoActionListener,
	                                final ModelStructureChangeListener modelStructureChangeListener) {
		this.undoActionListener = undoActionListener;
		this.modelViewManager = modelViewManager;
		this.modelStructureChangeListener = modelStructureChangeListener;
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
	public void setSelectedItem(FaceEffect itemToSelect) {
		faceEffect = itemToSelect;
		targetField.setText(faceEffect.getFaceEffectTarget());
		effectField.setText(faceEffect.getFaceEffect());

		revalidate();
		repaint();
	}

	@Override
	public void save(EditableModel model, UndoActionListener undoListener, ModelStructureChangeListener changeListener) {

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
