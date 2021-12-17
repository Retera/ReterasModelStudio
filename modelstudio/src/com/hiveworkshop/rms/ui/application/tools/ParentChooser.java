package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.IdObjectShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ParentChooser {
	IterableListModel<IdObjectShell<Bone>> filteredBones = new IterableListModel<>();
	IterableListModel<IdObjectShell<Bone>> posibleParentList;
	JList<IdObjectShell<Bone>> bonesJList;
	JTextField boneSearch;

	EditableModel model;

	public ParentChooser(EditableModel model) {
		this.model = model;
		posibleParentList = new IterableListModel<>();
	}

	public IdObject chooseParent(IdObject childObj, JComponent parent) {
//		BoneShell idObjBoneShell;
		IdObjectShell<Bone> parentBoneShell = null;
		posibleParentList.clear();
		posibleParentList.addElement(new IdObjectShell<>(null));
		for (Bone bone : model.getBones()) {
			IdObjectShell<Bone> boneShell = new IdObjectShell<>(bone);
			posibleParentList.addElement(boneShell);
			if (bone == childObj.getParent()) {
				parentBoneShell = boneShell;
			}
		}
		for (Helper bone : model.getHelpers()) {
			IdObjectShell<Bone> boneShell = new IdObjectShell<>(bone);
			posibleParentList.addElement(boneShell);
			if (bone == childObj.getParent()) {
				parentBoneShell = boneShell;
			}
		}

		JPanel boneChooserPanel = boneChooserPanel();
		bonesJList.setSelectedValue(parentBoneShell, true);


		int option = JOptionPane.showConfirmDialog(parent, boneChooserPanel, "Choose Bone", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (option == JOptionPane.OK_OPTION) {
			IdObjectShell<Bone> selectedValue = bonesJList.getSelectedValue();
			if (selectedValue != null) {
				return selectedValue.getIdObject();
			}
		}
		return childObj.getParent();
	}

	private JPanel boneChooserPanel() {
		JPanel panel = new JPanel(new MigLayout("fill, gap 0", "[grow]", "[][][grow]"));

		BoneShellListCellRenderer renderer = new BoneShellListCellRenderer(model, null).setShowClass(false);

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

	private void showParents(BoneShellListCellRenderer renderer, JCheckBox checkBox, JPanel panel) {
		renderer.setShowParent(checkBox.isSelected());
		panel.repaint();
	}

	private void filterBones() {
		String filterText = boneSearch.getText();
		if (!filterText.equals("")) {
			filteredBones.clear();
			for (IdObjectShell<Bone> boneShell : posibleParentList) {
				if (boneShell.getName().toLowerCase().contains(filterText.toLowerCase())) {
					filteredBones.addElement(boneShell);
				}
			}
			bonesJList.setModel(filteredBones);
		} else {
			bonesJList.setModel(posibleParentList);
		}
	}
}
