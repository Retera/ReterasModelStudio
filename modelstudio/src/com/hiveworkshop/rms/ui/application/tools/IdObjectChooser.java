package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.IdObjectListCellRenderer;
import com.hiveworkshop.rms.ui.util.SearchableList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IdObjectChooser {
	private final EditableModel model;
	private Set<Class<?>> classSet;
	private String title = "Choose Bone";
	private boolean isParentChooser;

	public IdObjectChooser(EditableModel model) {
		this.model = model;
	}
	public IdObjectChooser(EditableModel model, boolean isParentChooser) {
		this.model = model;
		this.isParentChooser = isParentChooser;
	}

	public IdObject chooseObject(IdObject childObj, JComponent parent) {
		IdObjectListCellRenderer renderer = new IdObjectListCellRenderer(model, null).setShowClass(false);

		SearchableList<IdObject> searchableList = getSearchableList();
		searchableList.setCellRenderer(renderer);

		if(isParentChooser && childObj != null){
			searchableList.setSelectedValue(childObj.getParent(), true);
		} else {
			searchableList.setSelectedValue(childObj, true);
		}

		JPanel boneChooserPanel = getChooserPanel(searchableList, renderer);

		int option = JOptionPane.showConfirmDialog(parent, boneChooserPanel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (option == JOptionPane.OK_OPTION) {
			IdObject selectedValue = searchableList.getSelectedValue();
			return selectedValue;
		}
		if (isParentChooser){
			return childObj != null ? childObj.getParent() : null;
		} else {
			return childObj;
		}
	}

	private SearchableList<IdObject> getSearchableList() {
		SearchableList<IdObject> searchableList = new SearchableList<>(this::idObjectNameFiler);

		searchableList.add(0, null);
		for (IdObject idObject : model.getIdObjects()) {
			if(classSet == null || classSet.contains(idObject.getClass())){
				searchableList.add(idObject);
			}
		}

		return searchableList;
	}

	private JPanel getChooserPanel(SearchableList<IdObject> searchableList, IdObjectListCellRenderer renderer) {
		JPanel panel = new JPanel(new MigLayout("fill, gap 0", "[grow]", "[][][grow]"));

		JCheckBox showParents = new JCheckBox("Show Parents");
		showParents.addActionListener(e -> showParents(renderer, showParents, panel));
		panel.add(showParents, "wrap");

		panel.add(searchableList.getSearchField(), "growx, wrap");
		panel.add(searchableList.getScrollableList(), "growx, growy, wrap");
		searchableList.scrollToSelected();
		return panel;
	}

	private void showParents(IdObjectListCellRenderer renderer, JCheckBox checkBox, JPanel panel) {
		renderer.setShowParent(checkBox.isSelected());
		panel.repaint();
	}

	private boolean idObjectNameFiler(IdObject objectShell, String filterText) {
		return (objectShell != null ? objectShell.getName() : "none").toLowerCase().contains(filterText.toLowerCase());
	}

	public IdObjectChooser setClassSet(Set<Class<?>> classSet) {
		this.classSet = classSet;
		return this;
	}
	public IdObjectChooser setClasses(Class<?>... clazzes) {
		this.classSet = new HashSet<>(List.of(clazzes));
		return this;
	}
}
