package com.etheller.util;

public final class StringUtils {
	public static int countOccurrences(final String haystack, final char needle) {
		int count = 0;
		for (int i = 0; i < haystack.length(); i++) {
			if (haystack.charAt(i) == needle) {
				count++;
			}
		}
		return count;
	}

	private StringUtils() {
	}
}
