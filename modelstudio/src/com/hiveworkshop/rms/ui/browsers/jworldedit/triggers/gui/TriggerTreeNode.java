package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui;

import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.Trigger;

import javax.swing.tree.DefaultMutableTreeNode;

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
