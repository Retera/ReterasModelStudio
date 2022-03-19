package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl;

import java.io.Serializable;

public abstract class Trigger implements Serializable {
	protected String name;
	protected String comment;
	protected TriggerCategory category;

	public Trigger(final String name) {
		this.name = name;
		this.comment = "";
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public TriggerCategory getCategory() {
		return category;
	}

	public void setCategory(final TriggerCategory category) {
		this.category = category;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

	public abstract boolean isComment();

	public abstract Trigger copy();
}
