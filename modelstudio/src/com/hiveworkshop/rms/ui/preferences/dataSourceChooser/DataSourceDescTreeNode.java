package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

public class DataSourceDescTreeNode<T extends DataSourceDescriptor> extends DataSourceTreeNode<T> {


	public DataSourceDescTreeNode(final T descriptor) {
		super(descriptor);
	}

	public void move(DefaultTreeModel model, int dir){
		MutableTreeNode parent = (MutableTreeNode) getParent();
		int newIndex = parent.getIndex(this) + dir;
		if (newIndex >= 0 && newIndex < parent.getChildCount()){
			model.removeNodeFromParent(this);
			model.insertNodeInto(this, parent, newIndex);
		}
	}
	public void remove(DefaultTreeModel model){
		model.removeNodeFromParent(this);
	}
}
