package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import com.hiveworkshop.rms.filesystem.sources.CascDataSourceDescriptor;

import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

public class CascDataSourceSubNode extends DataSourceTreeNode<CascDataSourceDescriptor> {

	private final CascDataSourceDescTreeNode parent;

	public CascDataSourceSubNode(String prefix, final CascDataSourceDescriptor descriptor, CascDataSourceDescTreeNode parent) {
		super(prefix, descriptor);
		this.parent = parent;
	}
	@Override
	public CascDataSourceDescriptor getDescriptor() {
		return null;
	}

	public void addDefaultCASCMod(DefaultTreeModel model, Component popupParent){
		parent.addDefaultCASCMod(model, popupParent);
	}

	public void addSpecificCASCMod(DefaultTreeModel model, Component popupParent){
		parent.addSpecificCASCMod(model, popupParent);
	}

	public void move(DefaultTreeModel model, int dir) {
		parent.move(model, dir, this);
	}
	public void remove(DefaultTreeModel model) {
		parent.remove(model, this);
	}
}
