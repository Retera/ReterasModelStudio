package com.hiveworkshop.wc3.jworldedit.triggers.impl;

public interface Trigger {
	TriggerCategory getCategory();

	void setCategory(TriggerCategory category);

	String getName();

	void setName(String name);

	boolean isComment();

	String getComment();

	void setComment(String comment);
}
