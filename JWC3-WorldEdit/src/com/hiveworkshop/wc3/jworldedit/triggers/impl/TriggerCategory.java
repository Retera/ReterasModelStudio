package com.hiveworkshop.wc3.jworldedit.triggers.impl;

import java.util.ArrayList;
import java.util.List;

public final class TriggerCategory {
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
}
