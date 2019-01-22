package com.hiveworkshop.wc3.jworldedit.triggers.gui;

import javax.swing.tree.MutableTreeNode;

public interface TriggerElementTreeNode extends MutableTreeNode {
	TriggerElementTreeNode copy();

	int getLevel();

	void add(MutableTreeNode node);
}
