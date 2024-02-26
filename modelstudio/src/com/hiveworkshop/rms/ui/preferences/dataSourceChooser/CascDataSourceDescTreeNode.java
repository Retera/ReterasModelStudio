package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import com.hiveworkshop.rms.filesystem.sources.CascDataSourceDescriptor;

import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.List;

public class CascDataSourceDescTreeNode extends DataSourceDescTreeNode<CascDataSourceDescriptor> {

	public CascDataSourceDescTreeNode(final CascDataSourceDescriptor descriptor) {
		super(descriptor);
		if (descriptor.getPrefixes().isEmpty()) {
			this.setUserObject(this.getUserObject() + " (WARNING: No Mods Selected)");
		}
		for (final String prefix : descriptor.getPrefixes()) {
			CascDataSourceSubNode cascChild = new CascDataSourceSubNode(prefix, descriptor, this);
			add(cascChild);
		}
	}

	public void addDefaultCASCMod(DefaultTreeModel model, Component popupParent) {
		List<String> prefixes = CascPrefixChooser.getDefaultCASCPrefixes(descriptor.getPath(), true, popupParent);
		if (prefixes != null) {
			addCASCMods(prefixes, model);
		}
	}

	public void addSpecificCASCMod(DefaultTreeModel model, Component popupParent) {
		List<String> prefixes = CascPrefixChooser.getSpecificPrefix(descriptor.getPath(), popupParent);
		if (prefixes != null) {
			addCASCMods(prefixes, model);
		}
	}

	public void addCASCMods(List<String> prefixes, DefaultTreeModel model) {
		descriptor.addPrefixes(prefixes);
		for (String prefix : prefixes) {
			CascDataSourceSubNode cascChild = new CascDataSourceSubNode(prefix, descriptor, this);
			model.insertNodeInto(cascChild, this, getChildCount());
		}
	}

	public void move(DefaultTreeModel model, int dir, CascDataSourceSubNode cascChild) {
		int index = getIndex(cascChild);
		descriptor.movePrefix(index, dir);
		int newIndex = index + dir;
		if (0 <= newIndex && newIndex < getChildCount()) {
			model.removeNodeFromParent(cascChild);
			model.insertNodeInto(cascChild, this, newIndex);
		}
	}
	public void remove(DefaultTreeModel model, CascDataSourceSubNode cascChild) {
		int index = getIndex(cascChild);
		descriptor.removePrefix(index);
		model.removeNodeFromParent(cascChild);
	}
}
