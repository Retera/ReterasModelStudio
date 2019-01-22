package com.hiveworkshop.wc3.units;

import java.awt.Color;
import java.awt.Image;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

public interface GameObject {

	public void setField(String field, String value);

	public void setField(String field, String value, int index);

	public String getField(String field);

	public String getField(String field, int index);

	public int getFieldValue(String field);

	public int getFieldValue(String field, int index);

	public List<? extends GameObject> getFieldAsList(String field, ObjectData objectData);

	public String getId();

	public ObjectData getTable();

	public String getName();

	public Set<String> keySet();

	public ImageIcon getScaledIcon(final double amt);

	public ImageIcon getScaledTintedIcon(final Color tint, final double amt);

	public Image getImage();
}
