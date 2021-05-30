package com.hiveworkshop.rms.parsers.slk;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;

public interface GameObject {

	void setField(String field, String value);

	void setField(String field, String value, int index);

	String getField(String field);

	String getField(String field, int index);

	int getFieldValue(String field);

	int getFieldValue(String field, int index);

	List<? extends GameObject> getFieldAsList(String field, ObjectData objectData);

	String getId();

	ObjectData getTable();

	String getName();

	Set<String> keySet();

	ImageIcon getScaledIcon(int size);

	ImageIcon getScaledTintedIcon(final Color tint, final int amt);

	Image getImage();
}
