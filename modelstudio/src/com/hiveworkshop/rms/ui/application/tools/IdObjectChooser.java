package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ObjectShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.ObjectShellListCellRenderer;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class IdObjectChooser {
	IterableListModel<ObjectShell> filteredBones = new IterableListModel<>();
	IterableListModel<ObjectShell> posibleParentList;
	JList<ObjectShell> bonesJList;
	JTextField boneSearch;

	EditableModel model;

	public IdObjectChooser(EditableModel model) {
		this.model = model;
		posibleParentList = new IterableListModel<>();
	}

	public IdObject chooseParent(IdObject childObj, JComponent parent) {
//		BoneShell idObjBoneShell;
		ObjectShell parentBoneShell = null;
		posibleParentList.clear();
		posibleParentList.addElement(new ObjectShell(null));
		for (IdObject idObject : model.getIdObjects()) {
			ObjectShell boneShell = new ObjectShell(idObject);
			posibleParentList.addElement(boneShell);
			if (idObject == childObj.getParent()) {
				parentBoneShell = boneShell;
			}
		}
//		for (Helper bone : model.getHelpers()) {
//			BoneShell boneShell = new BoneShell(bone);
//			posibleParentList.addElement(boneShell);
//			if (bone == childObj.getParent()) {
//				parentBoneShell = boneShell;
//			}
//		}

		JPanel boneChooserPanel = boneChooserPanel();
		bonesJList.setSelectedValue(parentBoneShell, true);


		int option = JOptionPane.showConfirmDialog(parent, boneChooserPanel, "Choose Bone", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (option == JOptionPane.OK_OPTION) {
			ObjectShell selectedValue = bonesJList.getSelectedValue();
			if (selectedValue != null) {
				return selectedValue.getIdObject();
			}
		}
		return childObj.getParent();
	}

	private JPanel boneChooserPanel() {
		JPanel panel = new JPanel(new MigLayout("fill, gap 0", "[grow]", "[][][grow]"));

		ObjectShellListCellRenderer renderer = new ObjectShellListCellRenderer(model, null).setShowClass(false);

		JCheckBox showParents = new JCheckBox("Show Parents");
		showParents.addActionListener(e -> showParents(renderer, showParents, panel));
		panel.add(showParents, "wrap");

		boneSearch = new JTextField();
		boneSearch.addCaretListener(e -> filterBones());
		panel.add(boneSearch, "growx, wrap");

		bonesJList = new JList<>(posibleParentList);
		bonesJList.setCellRenderer(renderer);

		panel.add(new JScrollPane(bonesJList), "growx, growy, wrap");
		return panel;
	}

	private void showParents(ObjectShellListCellRenderer renderer, JCheckBox checkBox, JPanel panel) {
		renderer.setShowParent(checkBox.isSelected());
		panel.repaint();
	}

	private void filterBones() {
		String filterText = boneSearch.getText();
		if (!filterText.equals("")) {
			filteredBones.clear();
			for (ObjectShell objectShell : posibleParentList) {
				if (objectShell.getName().toLowerCase().contains(filterText.toLowerCase())) {
					filteredBones.addElement(objectShell);
				}
			}
			bonesJList.setModel(filteredBones);
		} else {
			bonesJList.setModel(posibleParentList);
		}
	}
}
