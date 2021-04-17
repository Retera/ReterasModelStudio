package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class TreeNodeLinkerFromModel implements TreeNodeLinker {
	private final DefaultTreeModel treeModel;

	public TreeNodeLinkerFromModel(final DefaultTreeModel treeModel) {
		this.treeModel = treeModel;
	}

	@Override
	public void insertNodeInto(final DefaultMutableTreeNode newChild, final DefaultMutableTreeNode parent,
			final int index) {
		treeModel.insertNodeInto(newChild, parent, index);
	}

	@Override
	public void nodeChanged(final DefaultMutableTreeNode node) {
		treeModel.nodeChanged(node);
	}

	@Override
	public void removeNodeFromParent(final DefaultMutableTreeNode node) {
		treeModel.removeNodeFromParent(node);
	}
}