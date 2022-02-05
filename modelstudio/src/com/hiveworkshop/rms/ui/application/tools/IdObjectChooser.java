package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.IdObjectShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.ObjectShellListCellRenderer;
import com.hiveworkshop.rms.ui.util.SearchableList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class IdObjectChooser {
	private final EditableModel model;

	public IdObjectChooser(EditableModel model) {
		this.model = model;
	}

	public IdObject chooseParent(IdObject childObj, JComponent parent) {
		ObjectShellListCellRenderer renderer = new ObjectShellListCellRenderer(model, null).setShowClass(false);
		SearchableList<IdObjectShell<?>> bonesJList = getIdObjectShellList(childObj, renderer);

		JPanel boneChooserPanel = boneChooserPanel(bonesJList, renderer);

		int option = JOptionPane.showConfirmDialog(parent, boneChooserPanel, "Choose Bone", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (option == JOptionPane.OK_OPTION) {
			IdObjectShell<?> selectedValue = bonesJList.getSelectedValue();
			if (selectedValue != null) {
				return selectedValue.getIdObject();
			}
		}
		return childObj.getParent();
	}

	private SearchableList<IdObjectShell<?>> getIdObjectShellList(IdObject childObj, ObjectShellListCellRenderer renderer) {
		SearchableList<IdObjectShell<?>> bonesJList = new SearchableList<>(this::idObjectNameFiler);

		IdObjectShell<?> parentBoneShell = null;

		bonesJList.add(new IdObjectShell<>(null));
		for (IdObject idObject : model.getIdObjects()) {
			IdObjectShell<?> boneShell = new IdObjectShell<>(idObject);
			bonesJList.add(boneShell);
			if (idObject == childObj.getParent()) {
				parentBoneShell = boneShell;
			}
		}
		bonesJList.setSelectedValue(parentBoneShell, true);
		bonesJList.setCellRenderer(renderer);
		return bonesJList;
	}

	private JPanel boneChooserPanel(SearchableList<IdObjectShell<?>> bonesJList, ObjectShellListCellRenderer renderer) {
		JPanel panel = new JPanel(new MigLayout("fill, gap 0", "[grow]", "[][][grow]"));

		JCheckBox showParents = new JCheckBox("Show Parents");
		showParents.addActionListener(e -> showParents(renderer, showParents, panel));
		panel.add(showParents, "wrap");

		panel.add(bonesJList.getSearchField(), "growx, wrap");

		panel.add(bonesJList.getScrollableList(), "growx, growy, wrap");
		return panel;
	}

	private void showParents(ObjectShellListCellRenderer renderer, JCheckBox checkBox, JPanel panel) {
		renderer.setShowParent(checkBox.isSelected());
		panel.repaint();
	}

	private boolean idObjectNameFiler(IdObjectShell<?> objectShell, String filterText) {
		return objectShell.getName().toLowerCase().contains(filterText.toLowerCase());
	}
}
