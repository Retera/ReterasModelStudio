package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.actions.nodes.NameChangeAction;
import com.hiveworkshop.rms.editor.actions.nodes.ParentChangeAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import com.hiveworkshop.rms.ui.application.model.editors.QuatValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.Vec3ValuePanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ComponentHelperPanel extends ComponentPanel<Helper> {
	JLabel title;
	JTextField nameField;
	JLabel parentName;
	ParentChooser parentChooser;
	private Helper idObject;
	private Vec3ValuePanel transPanel;
	private Vec3ValuePanel scalePanel;
	private QuatValuePanel rotPanel;


	public ComponentHelperPanel(ModelHandler modelHandler, ModelStructureChangeListener changeListener) {
		super(modelHandler, changeListener);

		parentChooser = new ParentChooser(modelHandler.getModelView());

		setLayout(new MigLayout("fill, gap 0", "[][][grow]", "[][][grow]"));
		title = new JLabel("Select a Helper");
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
		transPanel = new Vec3ValuePanel(modelHandler, "Translation", modelHandler.getUndoManager(), changeListener);
		add(transPanel, "spanx, growx, wrap");
		scalePanel = new Vec3ValuePanel(modelHandler, "Scaling", modelHandler.getUndoManager(), changeListener);
		add(scalePanel, "spanx, growx, wrap");
		rotPanel = new QuatValuePanel(modelHandler, "Rotation", modelHandler.getUndoManager(), changeListener);
		add(rotPanel, "spanx, growx, wrap");
	}

	@Override
	public void setSelectedItem(Helper itemToSelect) {
		idObject = itemToSelect;
		title.setText(idObject.getName());
		nameField.setText(idObject.getName());
		IdObject parent = idObject.getParent();
		if (parent != null) {
			this.parentName.setText(parent.getName());
		} else {
			parentName.setText("no parent");
		}

		transPanel.reloadNewValue(new Vec3(0, 0, 0), (Vec3AnimFlag) idObject.find("Translation"), idObject, "Translation", null);

		scalePanel.reloadNewValue(new Vec3(1, 1, 1), (Vec3AnimFlag) idObject.find("Scaling"), idObject, "Scaling", null);

		rotPanel.reloadNewValue(new Quat(0, 0, 0, 1), (QuatAnimFlag) idObject.find("Rotation"), idObject, "Rotation", null);
		revalidate();
		repaint();

	}

	@Override
	public void save(EditableModel model, UndoManager undoManager, ModelStructureChangeListener changeListener) {

	}

	private void chooseParent() {
		IdObject newParent = parentChooser.chooseParent(idObject, this.getRootPane());
		ParentChangeAction action = new ParentChangeAction(idObject, newParent, changeListener);
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
					NameChangeAction action = new NameChangeAction(idObject, newName, changeListener);
					action.redo();
					modelHandler.getUndoManager().pushAction(action);
				}
			}
		};
	}
}
