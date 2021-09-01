package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.nodes.NameChangeAction;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ComponentCameraPanel extends ComponentPanel<Camera> {
	private final JLabel title;
	private final TwiTextField nameField;
	private Camera camera;

	public ComponentCameraPanel(ModelHandler modelHandler) {
		super(modelHandler);

		setLayout(new MigLayout("fill, gap 0", "[]5[]5[grow]", "[][][][grow]"));
		title = new JLabel("Select a Camera");
		add(title, "wrap");
		nameField = new TwiTextField(24, this::changeName1);
		add(nameField, "wrap");
	}

	@Override
	public void setSelectedItem(Camera itemToSelect) {
		camera = itemToSelect;
		title.setText(camera.getName());
		nameField.setText(camera.getName());

		revalidate();
		repaint();

	}

	private void changeName1(String newName) {
		if (!newName.equals("")) {
			undoManager.pushAction(new NameChangeAction(camera, newName, changeListener).redo());
		}
	}
}
