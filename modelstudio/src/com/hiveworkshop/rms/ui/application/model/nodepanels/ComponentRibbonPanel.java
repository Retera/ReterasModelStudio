package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.RibbonEmitter;
import com.hiveworkshop.rms.ui.application.actions.model.NameChangeAction;
import com.hiveworkshop.rms.ui.application.actions.model.ParentChangeAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ComponentRibbonPanel extends ComponentPanel<RibbonEmitter> {
	private final ModelHandler modelHandler;
	private final ModelStructureChangeListener modelStructureChangeListener;
	RibbonEmitter idObject;

	JLabel title;
	JTextField nameField;
	JLabel parentName;
	ParentChooser parentChooser;


	public ComponentRibbonPanel(ModelHandler modelHandler,
	                            ModelStructureChangeListener modelStructureChangeListener) {
		this.modelHandler = modelHandler;
		this.modelStructureChangeListener = modelStructureChangeListener;

		parentChooser = new ParentChooser(modelHandler.getModelView());

		setLayout(new MigLayout("fill, gap 0", "[][][grow]", "[][][grow]"));
		title = new JLabel("Select an Emitter");
		add(title, "wrap");
		nameField = new JTextField(24);
		nameField.addFocusListener(changeName());
		add(nameField, "wrap");
		add(new JLabel("Parent: "));
		parentName = new JLabel("Parent");
		add(parentName);
		JButton chooseParentButton = new JButton("change");
		chooseParentButton.addActionListener(e -> chooseParent());
		add(chooseParentButton, "wrap");
	}

	@Override
	public void setSelectedItem(RibbonEmitter itemToSelect) {
		idObject = itemToSelect;
		title.setText(idObject.getName());
		nameField.setText(idObject.getName());
		IdObject parent = idObject.getParent();
		if (parent != null) {
			this.parentName.setText(parent.getName());
		} else {
			parentName.setText("no parent");
		}
		revalidate();
		repaint();

	}

	@Override
	public void save(EditableModel model, UndoManager undoManager, ModelStructureChangeListener changeListener) {

	}

	private void chooseParent() {
		IdObject newParent = parentChooser.chooseParent(idObject, this.getRootPane());
		ParentChangeAction action = new ParentChangeAction(idObject, newParent, modelStructureChangeListener);
		action.redo();
		repaint();
		modelHandler.getUndoManager().pushAction(action);
	}

	private FocusAdapter changeName() {
		return new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String newName = nameField.getText();
				if (!newName.equals("")) {
					NameChangeAction action = new NameChangeAction(idObject, newName, modelStructureChangeListener);
					action.redo();
					modelHandler.getUndoManager().pushAction(action);
				}
			}
		};
	}
}
