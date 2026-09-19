package com.hiveworkshop.wc3.pkb;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** One serialized HBO object of a baked effect. */
public final class PkbObject {
	public final int index;
	public final String type;
	public final int flags;
	private final Map<String, PkbValue> fields = new LinkedHashMap<>();

	PkbObject(final int index, final String type, final int flags) {
		this.index = index;
		this.type = type;
		this.flags = flags;
	}

	void add(final PkbValue value) {
		fields.put(value.name, value);
	}

	public Map<String, PkbValue> getFields() {
		return Collections.unmodifiableMap(fields);
	}

	public PkbValue get(final String field) {
		return fields.get(field);
	}

	public boolean has(final String field) {
		return fields.containsKey(field);
	}

	public String getString(final String field, final String fallback) {
		final PkbValue value = fields.get(field);
		return value == null ? fallback : value.asString();
	}

	public float getFloat(final String field, final float fallback) {
		final PkbValue value = fields.get(field);
		return value == null ? fallback : value.asFloat();
	}

	public int getInt(final String field, final int fallback) {
		final PkbValue value = fields.get(field);
		return value == null ? fallback : value.asInt();
	}

	public boolean getBool(final String field, final boolean fallback) {
		final PkbValue value = fields.get(field);
		return value == null ? fallback : value.asBool();
	}

	public int getLink(final String field) {
		final PkbValue value = fields.get(field);
		return value == null ? -1 : value.asLink();
	}

	public int[] getLinks(final String field) {
		final PkbValue value = fields.get(field);
		return value == null ? new int[0] : value.asLinks();
	}

	/** The object's CustomName, when it has one. */
	public String getCustomName() {
		return getString("CustomName", "");
	}

	@Override
	public String toString() {
		return "$" + (index + 1) + " " + type + (getCustomName().isEmpty() ? "" : " \"" + getCustomName() + "\"");
	}
}
