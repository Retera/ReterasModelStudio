package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class BehaviorTreeNode extends DefaultMutableTreeNode {
	private final ImageIcon icon;

	public BehaviorTreeNode(final String text, final ImageIcon icon) {
		super(text);
		this.icon = icon;
	}

	public ImageIcon getIcon() {
		return icon;
	}
}
