package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

class ObjectPanel extends JPanel {
	JLabel title;

	IdObject object;
	Camera camera;
	JCheckBox doImport;
	JLabel parentLabel;
	JLabel oldParentLabel;
	IterableListModel<BoneShell> parents;
	JList<BoneShell> parentsList;
	JScrollPane parentsPane;
	ModelHolderThing mht;

	ObjectShell selectedObject;

	protected ObjectPanel() {

	}

	public ObjectPanel(ModelHolderThing mht, final IdObject whichObject, final IterableListModel<BoneShell> possibleParents) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0, ins 0", "[grow]", "[][][][][grow]"));
		setObject(whichObject);

		title = new JLabel("Object Title");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		doImport = new JCheckBox("Import this object");
		doImport.setSelected(true);
		add(doImport, "left, wrap");


		oldParentLabel = new JLabel("(Old Parent: {no parent})");
		add(oldParentLabel, "left, wrap");

		setTitles2();

		parentLabel = new JLabel("Parent:");
		add(parentLabel, "left, wrap");

		parents = possibleParents;
		parentsList = new JList<>();
		parentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		setParentListModel();
		selectParentInList2();

		parentsPane = new JScrollPane(parentsList);
		add(parentsPane, "growx, growy 200");
	}

	public ObjectPanel(ModelHolderThing mht) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0, ins 0", "[grow]", "[][][][][grow]"));

		title = new JLabel("Object Title");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		doImport = new JCheckBox("Import this object");
		doImport.setSelected(true);
		doImport.addActionListener(e -> setImportStatus());
		add(doImport, "left, wrap");


		oldParentLabel = new JLabel("(Old Parent: {no parent})");
		add(oldParentLabel, "left, wrap");


		parentLabel = new JLabel("Parent:");
		add(parentLabel, "left, wrap");

		parentsList = new JList<>();
		parentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		parentsPane = new JScrollPane(parentsList);
		add(parentsPane, "growx, growy 200");
	}

	public ObjectPanel setSelectedObject(ObjectShell selectedObject) {
		this.selectedObject = selectedObject;
		setObject(selectedObject.getIdObject());
		setTitles();
		parents = mht.getFutureBoneListExtended(true);
		setParentListModel();
		selectParentInList();
		setCheckboxStatus(selectedObject.getShouldImport());
		return this;
	}

	private void setObject(IdObject whichObject) {
		object = whichObject;
	}

	private void setParentListModel() {
		parentsList.setModel(parents);
	}

	private void selectParentInList() {
		System.out.println("obj: " + selectedObject + ", parent: " + selectedObject.getParent() + ", parents to choose from: " + parentsList.getModel().getSize());

		parentsList.setSelectedValue(selectedObject.getParent(), true);
		System.out.println("selected item: " + parentsList.getSelectedValue());
//		for (BoneShell bs : parents) {
////			if (bs.bone == object.getParent()) {
//			if (bs == selectedObject.getParent()) {
//				parentsList.setSelectedValue(bs, true);
//			}
//		}
	}

	private void selectParentInList2() {
		for (BoneShell bs : parents) {
			if (bs.bone == object.getParent()) {
//			if (bs == selectedObject.getParent()) {
				parentsList.setSelectedValue(bs, true);
			}
		}
	}

	private void setTitles() {
		title.setText(selectedObject.toString());
//		title.setText(object.getClass().getSimpleName() + " \"" + object.getName() + "\"");

		if (selectedObject.getParent() != null) {
			oldParentLabel.setText("(Old Parent: " + selectedObject.getParent().getName() + ")");
		} else {
			oldParentLabel.setText("(Old Parent: {no parent})");
		}
	}

	private void setTitles2() {
//		title.setText(object.getClass().getSimpleName() + " \"" + object.getName() + "\"");

		if (object.getParent() != null) {
			oldParentLabel.setText("(Old Parent: " + object.getParent().getName() + ")");
		} else {
			oldParentLabel.setText("(Old Parent: {no parent})");
		}
	}

	private void setCheckboxStatus(boolean isChecked) {
		doImport.setSelected(isChecked);
	}

	private void setImportStatus() {
		selectedObject.setShouldImport(doImport.isSelected());
	}

	public ObjectPanel(final Camera c) {
		setLayout(new MigLayout("gap 0", "[grow]", "[][][][][grow]"));
		camera = c;

		title = new JLabel(c.getClass().getSimpleName() + " \"" + c.getName() + "\"");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		doImport = new JCheckBox("Import this object");
		doImport.setSelected(true);
		add(doImport, "left, wrap");

		oldParentLabel = new JLabel("(Cameras don't have parents)");
		add(oldParentLabel, "left, wrap");

//		parentLabel = new JLabel("Parent:");
//		add(parentLabel, "left, wrap");
	}
}
