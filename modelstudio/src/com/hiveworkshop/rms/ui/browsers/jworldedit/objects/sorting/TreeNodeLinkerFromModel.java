package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class TreeNodeLinkerFromModel extends TreeNodeLinker {
	private final DefaultTreeModel treeModel;

	public TreeNodeLinkerFromModel(final DefaultTreeModel treeModel) {
		this.treeModel = treeModel;
	}

	@Override
	public void insertNodeInto(DefaultMutableTreeNode newChild, DefaultMutableTreeNode parent, int index) {
		treeModel.insertNodeInto(newChild, parent, index);
	}

	@Override
	public void nodeChanged(DefaultMutableTreeNode node) {
		treeModel.nodeChanged(node);
	}

	@Override
	public void removeNodeFromParent(DefaultMutableTreeNode node) {
		treeModel.removeNodeFromParent(node);
	}
}