package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class TriggerCategory implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 6165741794379834433L;
	private String name;
	private final List<Trigger> triggers;
	private boolean isComment;

	public TriggerCategory(final String name) {
		this.name = name;
		this.triggers = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public List<Trigger> getTriggers() {
		return triggers;
	}

	public boolean isComment() {
		return isComment;
	}

	public void setComment(final boolean isComment) {
		this.isComment = isComment;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public TriggerCategory copy() {
		final TriggerCategory triggerCategory = new TriggerCategory(name);
		triggerCategory.isComment = isComment;
		for (final Trigger trigger : triggers) {
			final Trigger copiedTrigger = trigger.copy();
			triggerCategory.triggers.add(copiedTrigger);
			copiedTrigger.setCategory(triggerCategory);
		}
		return triggerCategory;
	}
}
