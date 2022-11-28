package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.IdObjectShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.ui.util.SearchableList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class BoneChooser {

	private final EditableModel model;

	public BoneChooser(EditableModel model) {
		this.model = model;
	}

	public Bone chooseBone(Bone currentBone, JComponent parent) {
		BoneShellListCellRenderer renderer = new BoneShellListCellRenderer(model, null).setShowClass(false);

		SearchableList<IdObjectShell<Bone>> bonesJList = getBonesJList(currentBone, renderer);

		JPanel boneChooserPanel = boneChooserPanel(bonesJList, renderer);

		int option = JOptionPane.showConfirmDialog(parent, boneChooserPanel, "Choose Bone", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (option == JOptionPane.OK_OPTION) {
			IdObjectShell<Bone> selectedValue = bonesJList.getSelectedValue();
			if (selectedValue != null) {
				return selectedValue.getIdObject();
			}
		}
		return currentBone;
	}

	private SearchableList<IdObjectShell<Bone>> getBonesJList(Bone currentBone, BoneShellListCellRenderer renderer) {
		SearchableList<IdObjectShell<Bone>> bonesJList = new SearchableList<>(this::boneNameFilter);
		bonesJList.setCellRenderer(renderer);

		IdObjectShell<Bone> currentBoneShell = null;
		bonesJList.add(new IdObjectShell<>(null));
		for (Bone bone : model.getBones()) {
			IdObjectShell<Bone> boneShell = new IdObjectShell<>(bone);
			bonesJList.add(boneShell);
			if (bone == currentBone) {
				currentBoneShell = boneShell;
			}
		}
		bonesJList.setSelectedValue(currentBoneShell, true);
		return bonesJList;
	}

	private JPanel boneChooserPanel(SearchableList<IdObjectShell<Bone>> bonesJList, BoneShellListCellRenderer renderer) {
		JPanel panel = new JPanel(new MigLayout("fill, gap 0", "[grow]", "[][][grow]"));

		JCheckBox showParents = new JCheckBox("Show Parents");
		showParents.addActionListener(e -> showParents(renderer, showParents, panel));
		panel.add(showParents, "wrap");

		panel.add(bonesJList.getSearchField(), "growx, wrap");

		panel.add(new JScrollPane(bonesJList), "growx, growy, wrap");
		return panel;
	}

	private void showParents(BoneShellListCellRenderer renderer, JCheckBox checkBox, JPanel panel) {
		renderer.setShowParent(checkBox.isSelected());
		panel.repaint();
	}

	private boolean boneNameFilter(IdObjectShell<Bone> boneShell, String filterText) {
		return boneShell.getName().toLowerCase().contains(filterText.toLowerCase());
	}
}
