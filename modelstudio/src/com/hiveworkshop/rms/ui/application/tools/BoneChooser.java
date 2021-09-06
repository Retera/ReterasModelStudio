package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.BoneShell;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.BoneShellListCellRenderer;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class BoneChooser {
	IterableListModel<BoneShell> filteredBones = new IterableListModel<>();
	IterableListModel<BoneShell> posibleParentList;
	JList<BoneShell> bonesJList;
	JTextField boneSearch;

	EditableModel model;

	public BoneChooser(EditableModel model) {
		this.model = model;
		posibleParentList = new IterableListModel<>();
	}

	public Bone chooseBone(Bone currentBone, JComponent parent) {
		BoneShell currentBoneShell = null;
		posibleParentList.clear();
		posibleParentList.addElement(new BoneShell(null));
		for (Bone bone : model.getBones()) {
			BoneShell boneShell = new BoneShell(bone);
			posibleParentList.addElement(boneShell);
			if (bone == currentBone) {
				currentBoneShell = boneShell;
			}
		}
		for (Helper bone : model.getHelpers()) {
			BoneShell boneShell = new BoneShell(bone);
			posibleParentList.addElement(boneShell);
			if (bone == currentBone) {
				currentBoneShell = boneShell;
			}
		}

		JPanel boneChooserPanel = boneChooserPanel();
		bonesJList.setSelectedValue(currentBoneShell, true);


		int option = JOptionPane.showConfirmDialog(parent, boneChooserPanel, "Choose Bone", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (option == JOptionPane.OK_OPTION) {
			BoneShell selectedValue = bonesJList.getSelectedValue();
			if (selectedValue != null) {
				return selectedValue.getBone();
			}
		}
		return currentBone;
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
			for (BoneShell boneShell : posibleParentList) {
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
