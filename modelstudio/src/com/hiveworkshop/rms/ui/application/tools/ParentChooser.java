package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.IdObjectShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.ui.util.SearchableList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ParentChooser {
	private final EditableModel model;

	public ParentChooser(EditableModel model) {
		this.model = model;
	}

	public IdObject chooseParent(IdObject childObj, JComponent parent) {
		BoneShellListCellRenderer renderer = new BoneShellListCellRenderer(model, null).setShowClass(false);
		SearchableList<IdObjectShell<Bone>> bonesJList1 = makeBoneList(childObj, renderer);

		JPanel boneChooserPanel = boneChooserPanel(bonesJList1, renderer);

		int option = JOptionPane.showConfirmDialog(parent, boneChooserPanel, "Choose Bone", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (option == JOptionPane.OK_OPTION) {
			IdObjectShell<Bone> selectedValue = bonesJList1.getSelectedValue();
			if (selectedValue != null) {
				return selectedValue.getIdObject();
			}
		}
		return childObj.getParent();
	}

	private SearchableList<IdObjectShell<Bone>> makeBoneList(IdObject childObj, BoneShellListCellRenderer renderer) {
		IdObjectShell<Bone> parentBoneShell = null;
		SearchableList<IdObjectShell<Bone>> bonesJList1 = new SearchableList<>(this::idObjectNameFiler);
		bonesJList1.add(new IdObjectShell<>(null));
		for (Bone bone : model.getBones()) {
			IdObjectShell<Bone> boneShell = new IdObjectShell<>(bone);
			bonesJList1.add(boneShell);
			if (bone == childObj.getParent()) {
				parentBoneShell = boneShell;
			}
		}
		for (Helper bone : model.getHelpers()) {
			IdObjectShell<Bone> boneShell = new IdObjectShell<>(bone);
			bonesJList1.add(boneShell);
			if (bone == childObj.getParent()) {
				parentBoneShell = boneShell;
			}
		}

		bonesJList1.setRenderer(renderer);
		bonesJList1.setSelectedValue(parentBoneShell, true);
		return bonesJList1;
	}

	private JPanel boneChooserPanel(SearchableList<IdObjectShell<Bone>> bonesJList1, BoneShellListCellRenderer renderer) {
		JPanel panel = new JPanel(new MigLayout("fill, gap 0", "[grow]", "[][][grow]"));

		JCheckBox showParents = new JCheckBox("Show Parents");
		showParents.addActionListener(e -> showParents(renderer, showParents, panel));
		panel.add(showParents, "wrap");

		panel.add(bonesJList1.getSearchField(), "growx, wrap");
		panel.add(bonesJList1.getScrollableList(), "growx, growy, wrap");
		return panel;
	}

	private void showParents(BoneShellListCellRenderer renderer, JCheckBox checkBox, JPanel panel) {
		renderer.setShowParent(checkBox.isSelected());
		panel.repaint();
	}

	private boolean idObjectNameFiler(IdObjectShell<Bone> boneShell, String filterText) {
		return boneShell.getName().toLowerCase().contains(filterText.toLowerCase());
	}
}
