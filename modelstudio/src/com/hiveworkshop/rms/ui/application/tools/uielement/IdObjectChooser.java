package com.hiveworkshop.rms.ui.application.tools.uielement;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.util.NodeUtils;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.IdObjectListCellRenderer;
import com.hiveworkshop.rms.ui.util.SearchableTwiList;
import com.hiveworkshop.rms.util.uiFactories.FontHelper;
import com.hiveworkshop.rms.util.uiFactories.Label;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

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
		String currTitle = isParentChooser && childObj != null ? "Choose Parent For \"" + childObj.getName() + "\"" : "Choose Node";
		return chooseObject(childObj, currTitle, parent);
	}
	public IdObject chooseObject(IdObject childObj, String title, JComponent parent) {
		IdObjectListCellRenderer renderer = new IdObjectListCellRenderer(model, null).setShowClass(false);
		Set<IdObject> childSet = getChildSet(childObj);
		renderer.setInvalidObjects(childSet);

		List<IdObject> nodeList = getFilteredObjects(childObj, childSet, true);
		SearchableTwiList<IdObject> searchableList = new SearchableTwiList<>(nodeList, this::idObjectNameFiler);;
		searchableList.setCellRenderer(renderer);

		if(isParentChooser && childObj != null){
			searchableList.setSelectedValue(childObj.getParent(), true);
		} else {
			searchableList.setSelectedValue(childObj, true);
		}

		JPanel boneChooserPanel = getChooserPanel(searchableList, childObj, renderer);

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

	private List<IdObject> getFilteredObjects(IdObject childObj, Set<IdObject> childSet, boolean addChildren) {
		List<IdObject> nodeList = new ArrayList<>();
		nodeList.add(null);
		for (IdObject idObject : model.getIdObjects()) {
			if(classSet == null || classSet.contains(idObject.getClass())){
				if(!isParentChooser || idObject != childObj && !childSet.contains(idObject)) {
					nodeList.add(idObject);
				}
			}
		}
		if (addChildren){
			nodeList.addAll(childSet);
		}
		return nodeList;
	}

	private Set<IdObject> getChildSet(IdObject childObj) {
		Set<IdObject> childSet = new LinkedHashSet<>();
		if(isParentChooser && childObj != null){
			NodeUtils.collectChildren(childObj, childSet, false);
		}
		return childSet;
	}

	private JPanel getChooserPanel(SearchableTwiList<IdObject> searchableList, IdObject idObject, IdObjectListCellRenderer renderer) {
		JPanel panel = new JPanel(new MigLayout("fill, gap 0", "[grow]", "[][][grow]"));
		if (isParentChooser && idObject != null) {
			panel.add(new JLabel("Choose Parent For: "), "split");
			panel.add(Label.create(renderer.getImageIcon(idObject)));
			panel.add(FontHelper.set(Label.createSelectable(idObject.getName(), null, null, null), Font.BOLD, 16f), "wrap");
			String parentName = idObject.getParent() == null ? "None" : idObject.getParent().getName();
			panel.add(new JLabel("Current Parent: "), "split");
			panel.add(Label.createSelectable(parentName, null, null, null), "wrap");
		}

		JCheckBox showParents = new JCheckBox("Show Parents");
		showParents.addActionListener(e -> showParents(renderer, showParents, panel));
		panel.add(showParents, "span x, wrap");

		panel.add(searchableList.getSearchField(), "span x, growx, wrap");
		panel.add(searchableList.getScrollableList(), "span x, growx, growy, wrap");
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
