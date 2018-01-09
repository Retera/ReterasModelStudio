package com.hiveworkshop.wc3.jworldedit.triggers.impl;

public class TriggerImpl implements Trigger {
	private String name;
	private String comment;
	private TriggerCategory category;

	public TriggerImpl(final String name) {
		this.name = name;
		this.comment = "";
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isComment() {
		return false;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public void setComment(final String comment) {
		this.comment = comment;
	}

	@Override
	public TriggerCategory getCategory() {
		return category;
	}

	@Override
	public void setCategory(final TriggerCategory category) {
		this.category = category;
	}

}
