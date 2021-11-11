package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting;

import javax.swing.tree.DefaultMutableTreeNode;

public class PreModelCreationTreeNodeLinker extends TreeNodeLinker {
	@Override
	public void insertNodeInto(DefaultMutableTreeNode newChild, DefaultMutableTreeNode parent,
	                           final int index) {
		parent.insert(newChild, index);
	}

	@Override
	public void nodeChanged(DefaultMutableTreeNode node) {
		// no tree model
	}

	@Override
	public void removeNodeFromParent(DefaultMutableTreeNode node) {
		final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
		parent.remove(node);
	}
}