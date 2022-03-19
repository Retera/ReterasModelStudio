package com.hiveworkshop.rms.ui.util;

import com.hiveworkshop.rms.ui.preferences.GUITheme;
import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.acryl.AcrylLookAndFeel;
import com.jtattoo.plaf.aluminium.AluminiumLookAndFeel;
import com.jtattoo.plaf.hifi.HiFiLookAndFeel;
import com.jtattoo.plaf.noire.NoireLookAndFeel;

import javax.swing.*;
import java.util.Properties;

public final class EditorDisplayManager {

	public static void setupLookAndFeel(GUITheme theme) {

		// setup the look and feel properties
		final Properties props = new Properties();
		// props.put("windowDecoration", "false");
		//
		props.put("logoString", "RMS");
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

		switch (theme) {
			case DARK -> NoireLookAndFeel.setCurrentTheme(props);
			case HIFI -> HiFiLookAndFeel.setCurrentTheme(props);
			case ACRYL -> AcrylLookAndFeel.setCurrentTheme(props);
			case ALUMINIUM -> AluminiumLookAndFeel.setCurrentTheme(props);

		}
		Class<? extends AbstractLookAndFeel> themeClass = switch (theme) {
			case DARK -> NoireLookAndFeel.class;
			case HIFI -> HiFiLookAndFeel.class;
			case ACRYL -> AcrylLookAndFeel.class;
			case ALUMINIUM -> AluminiumLookAndFeel.class;
			default -> AluminiumLookAndFeel.class;
		};
//		System.out.println("class: " + themeClass);
		String themeClassName = themeClass.getName();
//		System.out.println("className: " + themeClassName);
		trySetTheme(themeClassName);
	}

	public static void trySetTheme(String themeClassName) {
		try {
			UIManager.setLookAndFeel(themeClassName);
		} catch (final ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.out.println("could not set theme");
			throw new RuntimeException(e);
		}
	}

	private EditorDisplayManager() {
	}
}
