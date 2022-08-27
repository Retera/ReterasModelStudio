package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

public abstract class DataSourceTreeNode<T extends DataSourceDescriptor> extends DefaultMutableTreeNode {

	protected final T descriptor;

	public DataSourceTreeNode(final T descriptor) {
		super(descriptor.getDisplayName());
		this.descriptor = descriptor;
	}
	public DataSourceTreeNode(final String prefix, final T descriptor) {
		super(prefix);
		this.descriptor = descriptor;
	}

	public T getDescriptor() {
		return descriptor;
	}
	public abstract void move(DefaultTreeModel model, int dir);
	public abstract void remove(DefaultTreeModel model);

	public void addDefaultCASCMod(DefaultTreeModel model, Component popupParent){
	}

	public void addSpecificCASCMod(DefaultTreeModel model, Component popupParent){
	}

	public boolean canMoveUp() {
		return 0 < getParent().getIndex(this);
	}

	public boolean canMoveDown() {
		return getParent().getIndex(this) < parent.getChildCount()-1;
	}
}
