package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public abstract class ComponentPanel<T> extends JPanel {
	protected final ModelHandler modelHandler;
	protected final ComponentsPanel componentsPanel;
	protected final EditableModel model;
	protected final UndoManager undoManager;
	protected final ModelStructureChangeListener changeListener;
	protected T selectedItem;

	public ComponentPanel(ModelHandler modelHandler, ComponentsPanel componentsPanel) {
		this.modelHandler = modelHandler;
		this.componentsPanel = componentsPanel;
		this.model = modelHandler.getModel();
		this.undoManager = modelHandler.getUndoManager();
		this.changeListener = ModelStructureChangeListener.changeListener;
	}

	public abstract ComponentPanel<T> setSelectedItem(T itemToSelect);

	protected JButton getDeleteButton(ActionListener actionListener) {
		JButton deleteButton = new JButton("Delete");
		deleteButton.setBackground(Color.RED);
		deleteButton.setForeground(Color.WHITE);
		deleteButton.addActionListener(actionListener);
		return deleteButton;
	}

	protected JButton getXButton(ActionListener actionListener) {
		JButton deleteButton = new JButton("X");
		deleteButton.setBackground(Color.RED);
		deleteButton.setForeground(Color.WHITE);
		deleteButton.addActionListener(actionListener);
		return deleteButton;
	}

	protected JButton getButton(String text, ActionListener actionListener) {
		JButton button = new JButton(text);
		button.addActionListener(actionListener);
		return button;
	}
}
