package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import com.hiveworkshop.rms.filesystem.sources.CascDataSourceDescriptor;

import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.nio.file.Paths;
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

	public void addDefaultCASCMod(DefaultTreeModel model, Component popupParent){
		List<String> prefixes = CascPrefixChooser.addDefaultCASCPrefixes(Paths.get(descriptor.getGameInstallPath()), true, popupParent);
		descriptor.addPrefixes(prefixes);
		for(String prefix : prefixes){
			CascDataSourceSubNode cascChild = new CascDataSourceSubNode(prefix, descriptor, this);
			model.insertNodeInto(cascChild, this, getChildCount());
		}
	}

	public void addSpecificCASCMod(DefaultTreeModel model, Component popupParent){
		List<String> prefixes = CascPrefixChooser.getSpecificPrefixs(Paths.get(descriptor.getGameInstallPath()), popupParent);
		descriptor.addPrefixes(prefixes);
		for(String prefix : prefixes){
			CascDataSourceSubNode cascChild = new CascDataSourceSubNode(prefix, descriptor, this);
			model.insertNodeInto(cascChild, this, getChildCount());
		}
	}

	public void move(DefaultTreeModel model, int dir, CascDataSourceSubNode cascChild) {
		int index = getIndex(cascChild);
		descriptor.movePrefix(index, dir);
		int newIndex = index + dir;
		if (newIndex >= 0 && newIndex < getChildCount()){
			model.removeNodeFromParent(cascChild);
			model.insertNodeInto(cascChild, this, newIndex);
		}
	}
	public void remove(DefaultTreeModel model, CascDataSourceSubNode cascChild) {
		int index = getIndex(cascChild);
		descriptor.deletePrefix(index);
		model.removeNodeFromParent(cascChild);
	}
}
