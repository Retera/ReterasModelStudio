package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting;

import javax.swing.tree.DefaultMutableTreeNode;

public abstract class TreeNodeLinker {
	public abstract void insertNodeInto(DefaultMutableTreeNode newChild, DefaultMutableTreeNode parent, int index);

	public abstract void nodeChanged(DefaultMutableTreeNode node);

	public abstract void removeNodeFromParent(DefaultMutableTreeNode node);
}
