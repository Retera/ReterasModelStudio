package com.hiveworkshop.wc3.util;

public final class IconUtils {
	private static final String DISABLED_PREFIX = "ReplaceableTextures\\CommandButtonsDisabled\\DIS";

	public static String getDisabledIcon(final String iconPath) {
		String iconName;
		if (iconPath.contains("\\")) {
			iconName = iconPath.substring(iconPath.lastIndexOf('\\') + 1);
		} else {
			iconName = iconPath;
		}
		return DISABLED_PREFIX + iconName;
	}

	private IconUtils() {
	}
}
