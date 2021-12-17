package com.hiveworkshop.rms.parsers.slk;

import java.util.Set;

public abstract class ObjectData {
	public abstract GameObject get(String id);

	public abstract void setValue(String id, String field, String value);

	public abstract Set<String> keySet();
}
