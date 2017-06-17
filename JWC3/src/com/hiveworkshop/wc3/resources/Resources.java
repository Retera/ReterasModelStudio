package com.hiveworkshop.wc3.resources;

import java.util.ResourceBundle;

public class Resources {
	static ResourceBundle bundle;

	static ResourceBundle get() {
		if (bundle == null) {
			bundle = ResourceBundle.getBundle(Resources.class.getName());
		}
		return bundle;
	}

	public static String getString(final String key) {
		return get().getString(key);
	}

	public static void dropCache() {
		bundle = null;
	}
}
