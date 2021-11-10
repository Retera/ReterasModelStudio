package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui;

import javax.swing.tree.DefaultMutableTreeNode;

public abstract class TriggerElementTreeNode extends DefaultMutableTreeNode {

	public TriggerElementTreeNode(String s) {
		super(s);
	}

	public abstract TriggerElementTreeNode copy();

//	public abstract int getLevel();

//	public abstract void add(MutableTreeNode node);

	public abstract TriggerElementTreeNode setNodeObjectName(String name);

	public abstract String getNodeObjectName();

}
