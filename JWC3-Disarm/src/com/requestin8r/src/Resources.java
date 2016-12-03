package com.requestin8r.src;

import java.util.ResourceBundle;

public class Resources {
	static ResourceBundle bundle;
	static ResourceBundle get() {
		if( bundle == null ) {
			bundle = ResourceBundle.getBundle(Resources.class.getName());
		}
		return bundle;
	}
	public static String getString(String key) {
		return get().getString(key);
	}
}
