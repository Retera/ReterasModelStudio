package com.hiveworkshop.assetripper;

import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.jtattoo.plaf.noire.NoireLookAndFeel;

public final class EditorDisplayManager {

	public static void setupLookAndFeel() {

		// setup the look and feel properties
		final Properties props = new Properties();
		// props.put("windowDecoration", "false");
		//
		props.put("logoString", "MatrixEater");
		// props.put("licenseKey", "INSERT YOUR LICENSE KEY HERE");
		//
		// props.put("selectionBackgroundColor", "180 240 197");
		// props.put("menuSelectionBackgroundColor", "180 240 197");
		//
		// props.put("controlColor", "218 254 230");
		// props.put("controlColorLight", "218 254 230");
		// props.put("controlColorDark", "180 240 197");
		//
		// props.put("buttonColor", "218 230 254");
		// props.put("buttonColorLight", "255 255 255");
		// props.put("buttonColorDark", "244 242 232");
		//
		// props.put("rolloverColor", "218 254 230");
		// props.put("rolloverColorLight", "218 254 230");
		// props.put("rolloverColorDark", "180 240 197");
		//
		// props.put("windowTitleForegroundColor", "0 0 0");
		// props.put("windowTitleBackgroundColor", "180 240 197");
		// props.put("windowTitleColorLight", "218 254 230");
		// props.put("windowTitleColorDark", "180 240 197");
		// props.put("windowBorderColor", "218 254 230");

		// set your theme
		NoireLookAndFeel.setCurrentTheme(props);
		// select the Look and Feel
		try {
			UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel");
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (final InstantiationException e) {
			throw new RuntimeException(e);
		} catch (final IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (final UnsupportedLookAndFeelException e) {
			throw new RuntimeException(e);
		}
	}

	private EditorDisplayManager() {
	}
}
