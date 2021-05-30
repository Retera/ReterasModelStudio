package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.model.header.SetBlendTimeAction;
import com.hiveworkshop.rms.editor.actions.model.header.SetFormatVersionAction;
import com.hiveworkshop.rms.editor.actions.model.header.SetHeaderExtentsAction;
import com.hiveworkshop.rms.editor.actions.model.header.SetNameAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;

public class ComponentHeaderPanel extends ComponentPanel<EditableModel> {
	private static final Dimension MAXIMUM_SIZE = new Dimension(99999, 25);
	private final ComponentEditorTextField modelNameField;
	private final ComponentEditorJSpinner formatVersionSpinner;
	private final ComponentEditorJSpinner blendTimeSpinner;
	private final ExtLogEditor extLogEditor;
	private final ModelHandler modelHandler;
	private final ModelStructureChangeListener changeListener;

	public ComponentHeaderPanel(ModelHandler modelHandler, ModelStructureChangeListener changeListener) {
		this.modelHandler = modelHandler;
		this.changeListener = changeListener;

		setLayout(new MigLayout("fill", "[]", "[][][][][][][][grow]"));

		JLabel modelNameLabel = new JLabel("Model Name:");
		modelNameField = new ComponentEditorTextField();
		modelNameField.setMaximumSize(MAXIMUM_SIZE);
		modelNameField.addActionListener(e -> modelNameField());

		JLabel versionLabel = new JLabel("Format Version:");
		formatVersionSpinner = new ComponentEditorJSpinner(new SpinnerNumberModel(800, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
		formatVersionSpinner.setMaximumSize(MAXIMUM_SIZE);
		formatVersionSpinner.addActionListener(this::formatVersionSpinner);

		JLabel blendTimeLabel = new JLabel("Blend Time:");
		blendTimeSpinner = new ComponentEditorJSpinner(new SpinnerNumberModel(150, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
		blendTimeSpinner.setMaximumSize(MAXIMUM_SIZE);
		blendTimeSpinner.addActionListener(this::blendTimeSpinner);

		extLogEditor = new ExtLogEditor();
		extLogEditor.setBorder(BorderFactory.createTitledBorder("Extents"));
		extLogEditor.addActionListener(this::setExtLog);

		add(new JLabel("Model Name:"), "wrap");
		add(modelNameField, "wrap, growx");
		add(new JLabel("Format Version:"), "wrap");
		add(formatVersionSpinner, "wrap, growx");
		add(new JLabel("Blend Time:"), "wrap");
		add(blendTimeSpinner, "wrap, growx");
		add(extLogEditor, "wrap, growx");

		GroupLayout layout = new GroupLayout(this);

//		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(modelNameLabel).addComponent(modelNameField)
//				.addComponent(versionLabel).addComponent(formatVersionSpinner).addComponent(blendTimeLabel)
//				.addComponent(blendTimeSpinner).addComponent(extLogEditor));
//		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(modelNameLabel).addComponent(modelNameField)
//				.addComponent(versionLabel).addComponent(formatVersionSpinner).addComponent(blendTimeLabel)
//				.addComponent(blendTimeSpinner).addComponent(extLogEditor));
//		setLayout(layout);
	}

	private void setExtLog() {
		System.out.println("edited: " + extLogEditor.getExtLog());
		SetHeaderExtentsAction setHeaderExtentsAction = new SetHeaderExtentsAction(
				modelHandler.getModel().getExtents(), extLogEditor.getExtLog(), modelHandler.getModelView(), changeListener);
		setHeaderExtentsAction.redo();
		modelHandler.getUndoManager().pushAction(setHeaderExtentsAction);
	}

	private void blendTimeSpinner() {
		if (modelHandler != null) {
			SetBlendTimeAction setFormatVersionAction =
					new SetBlendTimeAction(
							modelHandler.getModel().getBlendTime(),
							((Number) blendTimeSpinner.getValue()).intValue(),
							modelHandler.getModelView(),
							changeListener);
			setFormatVersionAction.redo();
			modelHandler.getUndoManager().pushAction(setFormatVersionAction);
		}
	}

	private void formatVersionSpinner() {
		if (modelHandler != null) {
			SetFormatVersionAction setFormatVersionAction =
					new SetFormatVersionAction(
							modelHandler.getModel().getFormatVersion(),
							((Number) formatVersionSpinner.getValue()).intValue(),
							modelHandler.getModelView(),
							changeListener);
			setFormatVersionAction.redo();
			modelHandler.getUndoManager().pushAction(setFormatVersionAction);
		}
	}

	private void modelNameField() {
		if (modelHandler != null) {
			SetNameAction action =
					new SetNameAction(
							modelHandler.getModel().getHeaderName(),
							modelNameField.getText(),
							modelHandler.getModelView(),
							changeListener);
			action.redo();
			modelHandler.getUndoManager().pushAction(action);
		}
	}

	private void setModelHeader(EditableModel model) {
		modelNameField.reloadNewValue(model.getHeaderName());
		formatVersionSpinner.reloadNewValue(model.getFormatVersion());
		blendTimeSpinner.reloadNewValue(model.getBlendTime());
		extLogEditor.setExtLog(model.getExtents());
	}

	@Override
	public void setSelectedItem(EditableModel model) {
//		commitEdits();
		modelNameField.reloadNewValue(model.getHeaderName());
		formatVersionSpinner.reloadNewValue(model.getFormatVersion());
		blendTimeSpinner.reloadNewValue(model.getBlendTime());
		extLogEditor.setExtLog(model.getExtents());
	}

	private void commitEdits() {
		try {
			formatVersionSpinner.commitEdit();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		try {
			blendTimeSpinner.commitEdit();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		extLogEditor.commitEdits();

	}

	@Override
	public void save(EditableModel modelOutput, UndoManager undoManager,
	                 ModelStructureChangeListener changeListener) {
		modelOutput.setFormatVersion(((Number) formatVersionSpinner.getValue()).intValue());
		modelOutput.setBlendTime(((Number) blendTimeSpinner.getValue()).intValue());
		modelOutput.setExtents(extLogEditor.getExtLog());
	}

}
