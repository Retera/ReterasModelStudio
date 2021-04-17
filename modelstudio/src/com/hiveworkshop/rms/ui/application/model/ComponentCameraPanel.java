package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.actions.model.NameChangeAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ComponentCameraPanel extends JPanel implements ComponentPanel<Camera> {
	private final ModelViewManager modelViewManager;
	private final UndoActionListener undoActionListener;
	private final ModelStructureChangeListener modelStructureChangeListener;
	JLabel title;
	JTextField nameField;
	private Camera camera;


	public ComponentCameraPanel(final ModelViewManager modelViewManager,
	                            final UndoActionListener undoActionListener,
	                            final ModelStructureChangeListener modelStructureChangeListener) {
		this.undoActionListener = undoActionListener;
		this.modelViewManager = modelViewManager;
		this.modelStructureChangeListener = modelStructureChangeListener;

		setLayout(new MigLayout("fill, gap 0", "[]5[]5[grow]", "[][][][grow]"));
		title = new JLabel("Select a Bone");
		add(title, "wrap");
		nameField = new JTextField(24);
		nameField.addFocusListener(changeName());
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

	@Override
	public void save(EditableModel model, UndoActionListener undoListener, ModelStructureChangeListener changeListener) {

	}

	private FocusAdapter changeName() {
		return new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String newName = nameField.getText();
				if (!newName.equals("")) {
					NameChangeAction action = new NameChangeAction(camera, newName, modelStructureChangeListener);
					action.redo();
					undoActionListener.pushAction(action);
				}
			}
		};
	}
}
