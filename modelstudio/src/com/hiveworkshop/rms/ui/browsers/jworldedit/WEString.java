package com.hiveworkshop.rms.ui.browsers.jworldedit;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class WEString {
	static ResourceBundle bundle;
	static ResourceBundle bundle_gs;

	static ResourceBundle get() {
		if (bundle == null) {
			bundle = getResourceBundle("UI\\WorldEditStrings.txt");
		}
		return bundle;
	}

	static ResourceBundle getGameStrings() {
		if (bundle_gs == null) {
			bundle_gs = getResourceBundle("UI\\WorldEditGameStrings.txt");
		}
		return bundle_gs;
	}

	private static PropertyResourceBundle getResourceBundle(String filepath) {
		try (final InputStream fis = GameDataFileSystem.getDefault().getResourceAsStream(filepath);
		     final InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
			return new PropertyResourceBundle(isr);
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getString(String string) {
		try {
			while (string.toUpperCase().startsWith("WESTRING")) {
				string = internalGetString(string);
			}
			return string;
		} catch (final MissingResourceException exc) {
			try {
				return getGameStrings().getString(string.toUpperCase());
			} catch (final MissingResourceException exc2) {
				return string;
			}
		}
	}

	private static String internalGetString(final String key) {
		try {
			String string = get().getString(key.toUpperCase());
			if (2 <= string.length()
					&& string.charAt(0) == '"'
					&& string.charAt(string.length() - 1) == '"') {
				// remove citation
				string = string.substring(1, string.length() - 1);
			}
			return string;
		} catch (final MissingResourceException exc) {
			return getGameStrings().getString(key.toUpperCase());
		}
	}

	public static String getStringCaseSensitive(final String key) {
		try {
			return get().getString(key);
		} catch (final MissingResourceException exc) {
			return getGameStrings().getString(key);
		}
	}

	public static void dropCache() {
		bundle = null;
		bundle_gs = null;
	}
}
