package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;

import javax.swing.tree.DefaultMutableTreeNode;

public class DataSourceDescTreeNode extends DefaultMutableTreeNode {

	private final DataSourceDescriptor descriptor;

	public DataSourceDescTreeNode(final DataSourceDescriptor descriptor) {
		super(descriptor.getDisplayName());
		this.descriptor = descriptor;
	}

	public DataSourceDescriptor getDescriptor() {
		return descriptor;
	}
}
