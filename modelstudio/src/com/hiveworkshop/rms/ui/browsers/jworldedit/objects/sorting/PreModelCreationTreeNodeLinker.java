package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting;

import javax.swing.tree.DefaultMutableTreeNode;

public class PreModelCreationTreeNodeLinker implements TreeNodeLinker {
	@Override
	public void insertNodeInto(final DefaultMutableTreeNode newChild, final DefaultMutableTreeNode parent,
			final int index) {
		parent.insert(newChild, index);
	}

	@Override
	public void nodeChanged(final DefaultMutableTreeNode node) {
		// no tree model
	}

	@Override
	public void removeNodeFromParent(final DefaultMutableTreeNode node) {
		final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
		parent.remove(node);
	}
}