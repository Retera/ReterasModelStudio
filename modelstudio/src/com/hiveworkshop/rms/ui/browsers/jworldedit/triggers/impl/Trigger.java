package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl;

public interface Trigger {
	TriggerCategory getCategory();

	void setCategory(TriggerCategory category);

	String getName();

	void setName(String name);

	boolean isComment();

	String getComment();

	void setComment(String comment);

	Trigger copy();
}
