package com.hiveworkshop.wc3.gui.modeledit.components;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;

import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.header.SetBlendTimeAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.header.SetFormatVersionAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.header.SetHeaderExtentsAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.header.SetNameAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.ComponentEditorJSpinner;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.ComponentEditorTextField;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

public class ComponentHeaderPanel extends JPanel implements ComponentPanel {
	private static final Dimension MAXIMUM_SIZE = new Dimension(99999, 25);
	private final ComponentEditorTextField modelNameField;
	private final ComponentEditorJSpinner formatVersionSpinner;
	private final ComponentEditorJSpinner blendTimeSpinner;
	private final ExtLogEditor extLogEditor;
	private ModelViewManager modelViewManager;
	private UndoActionListener undoActionListener;
	private ModelStructureChangeListener changeListener;

	public ComponentHeaderPanel() {
		final JLabel modelNameLabel = new JLabel("Model Name:");
		modelNameField = new ComponentEditorTextField();
		modelNameField.setMaximumSize(MAXIMUM_SIZE);
		modelNameField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (modelViewManager != null) {
					final SetNameAction action = new SetNameAction(modelViewManager.getModel().getHeaderName(),
							modelNameField.getText(), modelViewManager, changeListener);
					action.redo();
					undoActionListener.pushAction(action);
				}
			}
		});
		final JLabel versionLabel = new JLabel("Format Version:");
		formatVersionSpinner = new ComponentEditorJSpinner(
				new SpinnerNumberModel(800, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
		formatVersionSpinner.setMaximumSize(MAXIMUM_SIZE);
		formatVersionSpinner.addActionListener(new Runnable() {
			@Override
			public void run() {
				if (modelViewManager != null) {
					final SetFormatVersionAction setFormatVersionAction = new SetFormatVersionAction(
							modelViewManager.getModel().getFormatVersion(),
							((Number) formatVersionSpinner.getValue()).intValue(), modelViewManager, changeListener);
					setFormatVersionAction.redo();
					undoActionListener.pushAction(setFormatVersionAction);
				}
			}
		});
		final JLabel blendTimeLabel = new JLabel("Blend Time:");
		blendTimeSpinner = new ComponentEditorJSpinner(
				new SpinnerNumberModel(150, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
		blendTimeSpinner.setMaximumSize(MAXIMUM_SIZE);
		blendTimeSpinner.addActionListener(new Runnable() {
			@Override
			public void run() {
				if (modelViewManager != null) {
					final SetBlendTimeAction setFormatVersionAction = new SetBlendTimeAction(
							modelViewManager.getModel().getBlendTime(),
							((Number) blendTimeSpinner.getValue()).intValue(), modelViewManager, changeListener);
					setFormatVersionAction.redo();
					undoActionListener.pushAction(setFormatVersionAction);
				}
			}
		});

		extLogEditor = new ExtLogEditor();
		extLogEditor.setBorder(BorderFactory.createTitledBorder("Extents"));
		extLogEditor.addActionListener(new Runnable() {
			@Override
			public void run() {
				final SetHeaderExtentsAction setHeaderExtentsAction = new SetHeaderExtentsAction(
						modelViewManager.getModel().getExtents(), extLogEditor.getExtLog(), modelViewManager,
						changeListener);
				setHeaderExtentsAction.redo();
				undoActionListener.pushAction(setHeaderExtentsAction);
			}
		});

		final GroupLayout layout = new GroupLayout(this);

		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(modelNameLabel).addComponent(modelNameField)
				.addComponent(versionLabel).addComponent(formatVersionSpinner).addComponent(blendTimeLabel)
				.addComponent(blendTimeSpinner).addComponent(extLogEditor));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(modelNameLabel).addComponent(modelNameField)
				.addComponent(versionLabel).addComponent(formatVersionSpinner).addComponent(blendTimeLabel)
				.addComponent(blendTimeSpinner).addComponent(extLogEditor));
		setLayout(layout);
	}

	private void setModelHeader(final MDL model) {
		modelNameField.setText(model.getHeaderName());
		formatVersionSpinner.reloadNewValue(model.getFormatVersion());
		blendTimeSpinner.reloadNewValue(model.getBlendTime());
		extLogEditor.setExtLog(model.getExtents());
	}

	public void setActiveModel(final ModelViewManager modelViewManager, final UndoActionListener undoActionListener,
			final ModelStructureChangeListener changeListener) {
		commitEdits();
		this.modelViewManager = modelViewManager;
		this.undoActionListener = undoActionListener;
		this.changeListener = changeListener;
		setModelHeader(modelViewManager.getModel());
	}

	private void commitEdits() {
		try {
			formatVersionSpinner.commitEdit();
		} catch (final ParseException e) {
			e.printStackTrace();
		}
		try {
			blendTimeSpinner.commitEdit();
		} catch (final ParseException e) {
			e.printStackTrace();
		}
		extLogEditor.commitEdits();

	}

	@Override
	public void save(final MDL modelOutput, final UndoActionListener undoListener,
			final ModelStructureChangeListener changeListener) {
		modelOutput.setFormatVersion(((Number) formatVersionSpinner.getValue()).intValue());
		modelOutput.setBlendTime(((Number) blendTimeSpinner.getValue()).intValue());
		modelOutput.setExtents(extLogEditor.getExtLog());
	}

}
