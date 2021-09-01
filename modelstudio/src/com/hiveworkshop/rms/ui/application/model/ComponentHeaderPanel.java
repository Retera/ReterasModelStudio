package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.model.header.SetBlendTimeAction;
import com.hiveworkshop.rms.editor.actions.model.header.SetFormatVersionAction;
import com.hiveworkshop.rms.editor.actions.model.header.SetHeaderExtentsAction;
import com.hiveworkshop.rms.editor.actions.model.header.SetNameAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorTextField;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class ComponentHeaderPanel extends ComponentPanel<EditableModel> {
	private static final Dimension MAXIMUM_SIZE = new Dimension(99999, 25);
	private final ComponentEditorTextField modelNameField;
	private final IntEditorJSpinner formatVersionSpinner;
	private final IntEditorJSpinner blendTimeSpinner;
	private final ExtLogEditor extLogEditor;

	public ComponentHeaderPanel(ModelHandler modelHandler) {
		super(modelHandler);

		setLayout(new MigLayout("fill", "[]", "[][][][][][][][grow]"));

		modelNameField = new ComponentEditorTextField();
		modelNameField.setMaximumSize(MAXIMUM_SIZE);
		modelNameField.addActionListener(e -> setModelName(modelNameField.getText()));

		formatVersionSpinner = new IntEditorJSpinner(800, Integer.MIN_VALUE, this::setFormatVersion);
		formatVersionSpinner.setMaximumSize(MAXIMUM_SIZE);

		blendTimeSpinner = new IntEditorJSpinner(150, Integer.MIN_VALUE, this::setBlendTime);
		blendTimeSpinner.setMaximumSize(MAXIMUM_SIZE);

		extLogEditor = new ExtLogEditor();
		extLogEditor.setBorder(BorderFactory.createTitledBorder("Extents"));
		extLogEditor.addExtLogConsumer(this::setExtLog);

		add(new JLabel("Model Name:"), "wrap");
		add(modelNameField, "wrap, growx");
		add(new JLabel("Format Version:"), "wrap");
		add(formatVersionSpinner, "wrap, growx");
		add(new JLabel("Blend Time:"), "wrap");
		add(blendTimeSpinner, "wrap, growx");
		add(extLogEditor, "wrap, growx");
	}

	private void setExtLog(ExtLog extLog) {
		System.out.println("edited: " + extLog);
		undoManager.pushAction(new SetHeaderExtentsAction(extLog, model, changeListener).redo());
	}

	private void setBlendTime(int newBlendTime) {
		undoManager.pushAction(new SetBlendTimeAction(newBlendTime, model, changeListener).redo());
	}

	private void setFormatVersion(int newVersion) {
		undoManager.pushAction(new SetFormatVersionAction(newVersion, model, changeListener).redo());
	}

	private void setModelName(String newName) {
		undoManager.pushAction(new SetNameAction(newName, model, changeListener).redo());
	}

	@Override
	public void setSelectedItem(EditableModel model) {
		modelNameField.reloadNewValue(model.getHeaderName());
		formatVersionSpinner.reloadNewValue(model.getFormatVersion());
		blendTimeSpinner.reloadNewValue(model.getBlendTime());
		extLogEditor.setExtLog(model.getExtents());
	}
}
