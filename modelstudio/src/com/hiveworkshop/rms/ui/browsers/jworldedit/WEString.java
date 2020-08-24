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
	static ResourceBundle bundlegs;

	static ResourceBundle get() {
		if (bundle == null) {
			final InputStream fis;
			try {
				fis = GameDataFileSystem.getDefault().getResourceAsStream("UI\\WorldEditStrings.txt");
				try {
					bundle = new PropertyResourceBundle(new InputStreamReader(fis, StandardCharsets.UTF_8));
				} catch (final IOException e) {
					e.printStackTrace();
				} finally {
					fis.close();
				}
			} catch (final IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return bundle;
	}

	static ResourceBundle getGameStrings() {
		if (bundlegs == null) {
			final InputStream fis;
			try {
				fis = GameDataFileSystem.getDefault().getResourceAsStream("UI\\WorldEditGameStrings.txt");
				try {
					bundlegs = new PropertyResourceBundle(new InputStreamReader(fis, StandardCharsets.UTF_8));
				} catch (final IOException e) {
					e.printStackTrace();
				} finally {
					fis.close();
				}
			} catch (final IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return bundlegs;
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
			if ((string.charAt(0) == '"') && (string.length() >= 2) && (string.charAt(string.length() - 1) == '"')) {
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
		bundlegs = null;
	}
}
