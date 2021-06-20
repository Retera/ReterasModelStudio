package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.model.CollisionShape;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

public class ComponentCollisionPanel extends ComponentIdObjectPanel<CollisionShape> {
//	JLabel title;
//	JTextField nameField;
//	JLabel parentName;
//	ParentChooser parentChooser;
//	private CollisionShape idObject;


	public ComponentCollisionPanel(ModelHandler modelHandler, ModelStructureChangeListener changeListener) {
		super(modelHandler, changeListener);

//		parentChooser = new ParentChooser(modelHandler.getModelView());
//
//		setLayout(new MigLayout("fill, gap 0", "[][][grow]", "[][][grow]"));
//		title = new JLabel("Select a CollisionShape");
//		add(title, "wrap");
//		nameField = new JTextField(24);
//		nameField.addFocusListener(changeName());
//		add(nameField, "wrap");
//		add(new JLabel("Parent: "));
//		parentName = new JLabel("Parent");
//		add(parentName);
	}

//	@Override
//	public void setSelectedItem(CollisionShape itemToSelect) {
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
