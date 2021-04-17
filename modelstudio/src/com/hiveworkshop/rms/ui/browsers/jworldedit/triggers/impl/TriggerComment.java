package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl;

import java.io.Serializable;

public final class TriggerComment implements Trigger, Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -4246801166417351817L;
	private String name;
	private String comment;
	private TriggerCategory category;

	public TriggerComment(final String name) {
		this.name = name;
		this.comment = "";
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isComment() {
		return true;
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
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public TriggerCategory getCategory() {
		return category;
	}

	@Override
	public void setCategory(final TriggerCategory category) {
		this.category = category;
	}

	@Override
	public Trigger copy() {
		final TriggerComment triggerComment = new TriggerComment(name);
		triggerComment.comment = comment;
		return triggerComment;
	}

}
