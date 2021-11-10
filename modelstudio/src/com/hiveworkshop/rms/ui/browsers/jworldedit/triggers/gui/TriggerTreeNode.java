package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui;

import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.Trigger;

public class TriggerTreeNode extends TriggerElementTreeNode {

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
	public TriggerElementTreeNode setNodeObjectName(String name) {
		trigger.setName(name);
		return this;
	}

	@Override
	public String getNodeObjectName() {
		return trigger.getName();
	}

	@Override
	public String toString() {
		return "TTN: " + super.toString();
	}
}
