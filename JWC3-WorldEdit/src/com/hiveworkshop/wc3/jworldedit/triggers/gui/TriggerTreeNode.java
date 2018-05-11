package com.hiveworkshop.wc3.jworldedit.triggers.gui;

import javax.swing.tree.DefaultMutableTreeNode;

import com.hiveworkshop.wc3.jworldedit.triggers.impl.Trigger;

public class TriggerTreeNode extends DefaultMutableTreeNode implements TriggerElementTreeNode {

	private final Trigger trigger;

	public TriggerTreeNode(final Trigger trigger) {
		super(trigger.getName());
		this.trigger = trigger;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	@Override
	public TriggerElementTreeNode copy() {
		return new TriggerTreeNode(trigger);
	}

	@Override
	public String toString() {
		return "TTN: " + super.toString();
	}
}
