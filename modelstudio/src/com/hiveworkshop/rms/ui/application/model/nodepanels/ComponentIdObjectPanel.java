package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.actions.nodes.NameChangeAction;
import com.hiveworkshop.rms.editor.actions.nodes.ParentChangeAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
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


public abstract class ComponentIdObjectPanel<T extends IdObject> extends ComponentPanel<T> {
	private final ModelStructureChangeListener changeListener1;
	JLabel title;
	JTextField nameField;
	JLabel parentName;
	ParentChooser parentChooser;
	protected T idObject;
	protected Vec3ValuePanel transPanel;
	protected Vec3ValuePanel scalePanel;
	protected QuatValuePanel rotPanel;
	protected JLabel pivot;


	public ComponentIdObjectPanel(ModelHandler modelHandler,
	                              ModelStructureChangeListener changeListener) {
		super(modelHandler, changeListener);
		this.changeListener1 = changeListener;

		parentChooser = new ParentChooser(modelHandler.getModelView());

		setLayout(new MigLayout("fill, gap 0", "[]5[]5[grow]", "[][][][grow]"));
		title = new JLabel("Select a IdObject");
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

		pivot = new JLabel("(0.0,0.0,0.0)");
		add(new JLabel("pivot: "));
		add(pivot, "wrap");

		transPanel = new Vec3ValuePanel(modelHandler, MdlUtils.TOKEN_TRANSLATION, modelHandler.getUndoManager(), changeListener);
		scalePanel = new Vec3ValuePanel(modelHandler, MdlUtils.TOKEN_SCALING, modelHandler.getUndoManager(), changeListener);
		rotPanel = new QuatValuePanel(modelHandler, MdlUtils.TOKEN_ROTATION, modelHandler.getUndoManager(), changeListener);
		add(transPanel, "spanx, growx, wrap");
		add(scalePanel, "spanx, growx, wrap");
		add(rotPanel, "spanx, growx, wrap");
	}

	@Override
	public void setSelectedItem(T itemToSelect) {
		idObject = itemToSelect;
		title.setText(idObject.getName());
		nameField.setText(idObject.getName());
		IdObject parent = idObject.getParent();
		if (parent != null) {
			this.parentName.setText(parent.getName());
		} else {
			parentName.setText("no parent");
		}

		transPanel.reloadNewValue(new Vec3(0, 0, 0), (Vec3AnimFlag) idObject.find(MdlUtils.TOKEN_TRANSLATION), idObject, MdlUtils.TOKEN_TRANSLATION, null);
		scalePanel.reloadNewValue(new Vec3(1, 1, 1), (Vec3AnimFlag) idObject.find(MdlUtils.TOKEN_SCALING), idObject, MdlUtils.TOKEN_SCALING, null);
		rotPanel.reloadNewValue(new Quat(0, 0, 0, 1), (QuatAnimFlag) idObject.find(MdlUtils.TOKEN_ROTATION), idObject, MdlUtils.TOKEN_ROTATION, null);

		updatePanels();
		revalidate();
		repaint();

	}

	public void updatePanels() {
	}

	public void save(EditableModel model, UndoManager undoManager, ModelStructureChangeListener changeListener) {

	}

	private void chooseParent() {
		IdObject newParent = parentChooser.chooseParent(idObject, this.getRootPane());
		ParentChangeAction action = new ParentChangeAction(idObject, newParent, changeListener1);
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
					NameChangeAction action = new NameChangeAction(idObject, newName, changeListener1);
					action.redo();
					modelHandler.getUndoManager().pushAction(action);
				}
			}
		};
	}
}
