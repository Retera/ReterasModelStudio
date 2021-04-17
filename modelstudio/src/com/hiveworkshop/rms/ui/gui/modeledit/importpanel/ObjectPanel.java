package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;

class ObjectPanel extends JPanel {
	JLabel title;

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

	public ObjectPanel(ModelHolderThing mht, BoneShellListCellRenderer bonePanelRenderer) {
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
		parentsList.setCellRenderer(bonePanelRenderer);
		parentsList.addListSelectionListener(this::setParent);

		parentsPane = new JScrollPane(parentsList);
		add(parentsPane, "growx, growy 200");
	}

	public ObjectPanel setSelectedObject(ObjectShell selectedObject) {
		this.selectedObject = selectedObject;
		parentsList.setEnabled(selectedObject.getCamera() == null);
		setTitles();
		parents = mht.getFutureBoneListExtended(true);
		setParentListModel();
		setCheckboxStatus(selectedObject.getShouldImport());
		return this;
	}

	private void setParentListModel() {
		parentsList.setModel(parents);
	}

	private void setParent(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting() && parentsList.getSelectedValue() != null) {
			if (parentsList.getSelectedValue() == selectedObject.getNewParentBs()) {
				selectedObject.setNewParentBs(null);
			} else {
				selectedObject.setNewParentBs(parentsList.getSelectedValue());
			}
		}
	}


	private void setTitles() {
		title.setText(selectedObject.toString());
//		title.setText(object.getClass().getSimpleName() + " \"" + object.getName() + "\"");

		if (selectedObject.getOldParentBs() != null) {
			oldParentLabel.setText("(Old Parent: " + selectedObject.getOldParentBs().getName() + ")");
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

}
