package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl;

import java.io.Serializable;

public class TriggerImpl implements Trigger, Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -878578198686089131L;
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

	@Override
	public Trigger copy() {
		final TriggerImpl triggerImpl = new TriggerImpl(name);
		triggerImpl.comment = comment;
		// TODO copy trigger data
		return triggerImpl;
	}

}
