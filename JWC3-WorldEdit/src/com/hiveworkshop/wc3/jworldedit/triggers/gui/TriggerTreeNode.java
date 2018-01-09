package com.hiveworkshop.wc3.jworldedit.triggers.gui;

import javax.swing.tree.DefaultMutableTreeNode;

import com.hiveworkshop.wc3.jworldedit.triggers.impl.Trigger;

public class TriggerTreeNode extends DefaultMutableTreeNode {

	private final Trigger trigger;

	public TriggerTreeNode(final Trigger trigger) {
		super(trigger.getName());
		this.trigger = trigger;
	}

	public Trigger getTrigger() {
		return trigger;
	}
}
