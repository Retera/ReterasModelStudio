package com.requestin8r.src;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.matrixeater.src.MPQHandler;

public class WEString {
	static ResourceBundle bundle;
	static ResourceBundle bundlegs;
	static ResourceBundle get() {
		if( bundle == null ) {
			File temp = MPQHandler.get().getGameFile("UI\\WorldEditStrings.txt");
			FileInputStream fis;
			try {
				fis = new FileInputStream(temp);
				try {
					bundle = new PropertyResourceBundle(fis);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return bundle;
	}
	static ResourceBundle getGameStrings() {
		if( bundlegs == null ) {
			File temp = MPQHandler.get().getGameFile("UI\\WorldEditGameStrings.txt");
			FileInputStream fis;
			try {
				fis = new FileInputStream(temp);
				try {
					bundlegs = new PropertyResourceBundle(fis);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return bundlegs;
	}
	public static String getString(String key) {
		try {
			return get().getString(key.toUpperCase());
		} catch (MissingResourceException exc) {
			return getGameStrings().getString(key.toUpperCase());
		}
	}
}
