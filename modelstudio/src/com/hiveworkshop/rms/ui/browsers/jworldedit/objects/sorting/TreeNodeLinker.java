package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting;

import javax.swing.tree.DefaultMutableTreeNode;

public interface TreeNodeLinker {
	void insertNodeInto(DefaultMutableTreeNode newChild, DefaultMutableTreeNode parent, int index);

	void nodeChanged(DefaultMutableTreeNode node);

	void removeNodeFromParent(DefaultMutableTreeNode node);
}
