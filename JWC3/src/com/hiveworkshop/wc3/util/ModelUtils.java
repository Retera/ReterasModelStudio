package com.hiveworkshop.wc3.util;

public final class ModelUtils {
	public static String getPortrait(final String filepath) {
		final String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait"
				+ filepath.substring(filepath.lastIndexOf('.'), filepath.length());
		return portrait;
	}

	private ModelUtils() {
	}
}
