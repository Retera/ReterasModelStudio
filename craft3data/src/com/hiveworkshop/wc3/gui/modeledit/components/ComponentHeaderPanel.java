package com.hiveworkshop.wc3.gui.modeledit.components;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.mdl.MDL;

public class ComponentHeaderPanel extends JPanel implements ComponentPanel {
	private final JTextField modelNameField;
	private final JSpinner formatVersionSpinner;
	private final JSpinner blendTimeSpinner;
	private final ExtLogEditor extLogEditor;

	public ComponentHeaderPanel() {
		final JLabel modelNameLabel = new JLabel("Model Name:");
		modelNameField = new JTextField();
		final JLabel versionLabel = new JLabel("Format Version:");
		formatVersionSpinner = new JSpinner(new SpinnerNumberModel(800, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
		final JLabel blendTimeLabel = new JLabel("Blend Time:");
		blendTimeSpinner = new JSpinner(new SpinnerNumberModel(150, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));

		extLogEditor = new ExtLogEditor();
		extLogEditor.setBorder(BorderFactory.createTitledBorder("Extents"));

		final GroupLayout layout = new GroupLayout(this);

		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(modelNameLabel).addComponent(modelNameField)
				.addComponent(versionLabel).addComponent(formatVersionSpinner).addComponent(blendTimeLabel)
				.addComponent(blendTimeSpinner).addComponent(extLogEditor));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(modelNameLabel).addComponent(modelNameField)
				.addComponent(versionLabel).addComponent(formatVersionSpinner).addComponent(blendTimeLabel)
				.addComponent(blendTimeSpinner).addComponent(extLogEditor));
		setLayout(layout);
	}

	public void setModelHeader(final MDL model) {
		modelNameField.setText(model.getHeaderName());
		formatVersionSpinner.setValue(model.getFormatVersion());
		blendTimeSpinner.setValue(model.getBlendTime());
		extLogEditor.setExtLog(model.getExtents());
	}

	@Override
	public void save(final MDL modelOutput, final UndoActionListener undoListener,
			final ModelStructureChangeListener changeListener) {
		modelOutput.setName(modelNameField.getText());
		modelOutput.setFormatVersion(((Number) formatVersionSpinner.getValue()).intValue());
		modelOutput.setBlendTime(((Number) blendTimeSpinner.getValue()).intValue());
		modelOutput.setExtents(extLogEditor.getExtLog());
	}

}
