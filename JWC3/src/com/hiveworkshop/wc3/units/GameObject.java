package com.hiveworkshop.wc3.units;

import java.util.List;
import java.util.Set;

public interface GameObject {

	public void setField(String field, String value);
	public String getField(String field);
	public int getFieldValue(String field);
	public List<? extends GameObject> getFieldAsList(String field, ObjectData objectData);
	public String getId();
	public ObjectData getTable();
	public String getName();
	public Set<String> keySet();
}
