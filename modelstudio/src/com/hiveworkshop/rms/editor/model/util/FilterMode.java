package com.hiveworkshop.rms.editor.model.util;

public enum FilterMode {
	NONE("None"),
	TRANSPARENT("Transparent"),
	BLEND("Blend"),
	ADDITIVE("Additive"),
	ADDALPHA("AddAlpha"),
	MODULATE("Modulate"),
	MODULATE2X("Modulate2x");

	private final String token;

	FilterMode(final String token) {
		this.token = token;
	}

	public static FilterMode fromId(final int id) {
		return values()[id];
	}

	public static int nameToId(final String name) {
		for (final FilterMode mode : values()) {
			if (mode.token.equals(name)) {
				return mode.ordinal();
			}
		}
		return -1;
	}

	public static FilterMode nameToFilter(final String name) {
		for (final FilterMode mode : values()) {
			if (mode.token.equals(name)) {
				return mode;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return token;
	}
}
