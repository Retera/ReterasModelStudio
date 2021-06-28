package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.model.Attachment;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

public class ComponentAttatchmentPanel extends ComponentIdObjectPanel<Attachment> {
//	Attachment idObject;
//
//	JLabel title;
//	JTextField nameField;
//	JLabel parentName;
//	ParentChooser parentChooser;


	public ComponentAttatchmentPanel(ModelHandler modelHandler) {
		super(modelHandler);

//		parentChooser = new ParentChooser(modelHandler.getModelView());

//		setLayout(new MigLayout("fill, gap 0", "[][][grow]", "[][][grow]"));
//		title = new JLabel("Select an Emitter");
//		add(title, "wrap");
//		nameField = new JTextField(24);
//		nameField.addFocusListener(changeName());
//		add(nameField, "wrap");
//		add(new JLabel("Parent: "));
//		parentName = new JLabel("Parent");
//		add(parentName);
//		JButton chooseParentButton = new JButton("change");
//		chooseParentButton.addActionListener(e -> chooseParent());
//		add(chooseParentButton, "wrap");

	}

//	@Override
//	public void setSelectedItem(Attachment itemToSelect) {
//		idObject = itemToSelect;
//		title.setText(idObject.getName());
//		nameField.setText(idObject.getName());
//		IdObject parent = idObject.getParent();
//		if (parent != null) {
//			this.parentName.setText(parent.getName());
//		} else {
//			parentName.setText("no parent");
//		}
//		revalidate();
//		repaint();
//
//	}

//	@Override
//	public void save(EditableModel model, UndoManager undoManager, ModelStructureChangeListener changeListener) {
//
//	}

//	private void chooseParent() {
//		IdObject newParent = parentChooser.chooseParent(idObject, this.getRootPane());
//		ParentChangeAction action = new ParentChangeAction(idObject, newParent, changeListener);
//		action.redo();
//		repaint();
//		modelHandler.getUndoManager().pushAction(action);
//	}
//
//	private FocusAdapter changeName() {
//		return new FocusAdapter() {
//			@Override
//			public void focusLost(FocusEvent e) {
//				String newName = nameField.getText();
//				if (!newName.equals("")) {
//					NameChangeAction action = new NameChangeAction(idObject, newName, changeListener);
//					action.redo();
//					modelHandler.getUndoManager().pushAction(action);
//				}
//			}
//		};
//	}
}
