package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui;

import javax.swing.tree.MutableTreeNode;

public interface TriggerElementTreeNode extends MutableTreeNode {
	TriggerElementTreeNode copy();

	int getLevel();

	void add(MutableTreeNode node);
}
