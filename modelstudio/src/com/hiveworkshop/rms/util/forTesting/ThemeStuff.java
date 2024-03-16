package com.hiveworkshop.rms.util.forTesting;

import com.hiveworkshop.rms.ui.preferences.GUITheme;
import com.jtattoo.plaf.acryl.AcrylLookAndFeel;
import com.jtattoo.plaf.aluminium.AluminiumLookAndFeel;
import com.jtattoo.plaf.hifi.HiFiLookAndFeel;
import com.jtattoo.plaf.noire.NoireLookAndFeel;
import net.infonode.gui.laf.InfoNodeLookAndFeel;
import net.infonode.gui.laf.InfoNodeLookAndFeelThemes;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;

public class ThemeStuff {

	public static JTable getJTable(Hashtable<?, ?> defaults, String keyFilter, String valueFilter) {
		System.out.println(defaults.size() + " properties defined !");
		String[] colName = {"Key", "Value"};
		String[][] rowData = new String[defaults.size()][2];

		if (keyFilter == null || keyFilter.isBlank()) {
			System.out.println("no key filter!");
			keyFilter = ".*";
		}
		if (valueFilter == null || valueFilter.isBlank()) {
			System.out.println("no value filter!");
			valueFilter = ".*";
		}

		int i = 0;
		for (Enumeration<?> e = defaults.keys(); e.hasMoreElements(); i++) {
			Object key = e.nextElement();
			String keyString = key.toString();
			if (keyString.matches(keyFilter)) {
				Object value = defaults.get(key);
				String valueString = "" +  value;
				if (valueString.matches(valueFilter)) {
					rowData[i][0] = keyString;
					if (defaults.get(key) instanceof Object[] data) {
						System.out.println("_______ARRAY_______");
						rowData[i][1] = "" + Arrays.toString(data);
					} else if (defaults.get(key) instanceof Collection<?> collection) {
						System.out.println("_______Collection_______");
						rowData[i][1] = "" + Arrays.toString(collection.toArray(new Object[0]));
					} else {
						rowData[i][1] = "" + defaults.get(key);
					}
					System.out.println(keyString + "\t - \t\"" + value + "\"");
				}
			}
		}
		return new JTable(rowData, colName);
	}

	public static JTable getJTable(UIDefaults defaults) {
		System.out.println(defaults.size() + " properties defined !");
		String[] colName = {"Key", "Value"};
		String[][] rowData = new String[defaults.size()][2];

		int i = 0;
		for (Enumeration<?> e = defaults.keys(); e.hasMoreElements(); i++) {
			Object key = e.nextElement();
			rowData[i][0] = key.toString();
			if (defaults.get(key) instanceof Object[] data) {
				System.out.println("_______ARRAY_______");
				rowData[i][1] = "" + Arrays.toString(data);
			} else if (defaults.get(key) instanceof Collection<?> collection) {
				System.out.println("_______Collection_______");
				rowData[i][1] = "" + Arrays.toString(collection.toArray(new Object[0]));
			} else {
				rowData[i][1] = "" + defaults.get(key);
			}
			System.out.println(rowData[i][0] + "\t\t - \t\t" + rowData[i][1]);
		}
		return new JTable(rowData, colName);
	}

	public static void printDefaults(UIDefaults defaults) {
		System.out.println(defaults.size() + " properties defined !");
		for (Enumeration<?> e = defaults.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			String settingsString;
			if (defaults.get(key) instanceof Object[]) {
				System.out.println("_______ARRAY_______");
				settingsString = "" + Arrays.toString((Object[]) defaults.get(key));
			} else if (defaults.get(key) instanceof Collection) {
				System.out.println("_______Collection_______");
				settingsString = "" + Arrays.toString(((Collection<?>) defaults.get(key)).toArray(new Object[0]));
			} else {
				settingsString = "" + defaults.get(key);
			}
			System.out.println(key.toString() + "\t\t - \t\t" + settingsString);
		}
	}


	public static void printUIDefaults(Hashtable<?, ?> defaults, String keyFilter, String valueFilter) {
		System.out.println(defaults.size() + " properties defined !");
		if (keyFilter == null || keyFilter.isBlank() || keyFilter.isEmpty()) {
			System.out.println("no key filter!");
			keyFilter = ".*";
		}
		if (valueFilter == null || valueFilter.isBlank() || valueFilter.isEmpty()) {
			System.out.println("no value filter!");
			valueFilter = ".*";
		}
		int i = 0;
		for (Enumeration<?> e = defaults.keys(); e.hasMoreElements(); i++) {
			Object key = e.nextElement();
			String keyString = key.toString();
			if (keyString.matches(keyFilter)) {
				Object value = defaults.get(key);
				String valueString = "" +  value;
				if (valueString.matches(valueFilter)) {
					System.out.println(keyString + "\t - \t\"" + value + "\"");
				}
			}
		}
	}

	public static void setTheme(int themeNum) {
		try {
			switch (themeNum) {
				case 0 -> UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				case 1 -> UIManager.setLookAndFeel(NoireLookAndFeel.class.getName());
				case 2 -> UIManager.setLookAndFeel(HiFiLookAndFeel.class.getName());
				case 3 -> UIManager.setLookAndFeel(AcrylLookAndFeel.class.getName());
				case 4 -> UIManager.setLookAndFeel(AluminiumLookAndFeel.class.getName());
				case 5 -> UIManager.setLookAndFeel(new InfoNodeLookAndFeel(InfoNodeLookAndFeelThemes.getSoftGrayTheme()));
				case 6 -> UIManager.setLookAndFeel(new InfoNodeLookAndFeel(InfoNodeLookAndFeelThemes.getBlueIceTheme()));
				case 7 -> UIManager.setLookAndFeel(new InfoNodeLookAndFeel(InfoNodeLookAndFeelThemes.getDarkBlueGreenTheme()));
				case 8 -> UIManager.setLookAndFeel(new InfoNodeLookAndFeel(InfoNodeLookAndFeelThemes.getGrayTheme()));
			}
		} catch (final UnsupportedLookAndFeelException
		               | ClassNotFoundException
		               | InstantiationException
		               | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static void setTheme(GUITheme theme) {
		try {
			switch (theme) {
				case FOREST_GREEN -> {}
				case JAVA_DEFAULT -> {}
				case DARK -> UIManager.setLookAndFeel(NoireLookAndFeel.class.getName());
				case WINDOWS -> UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				case WINDOWS_CLASSIC -> UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
				case SOFT_GRAY -> UIManager.setLookAndFeel(new InfoNodeLookAndFeel(InfoNodeLookAndFeelThemes.getSoftGrayTheme()));
				case BLUE_ICE -> UIManager.setLookAndFeel(new InfoNodeLookAndFeel(InfoNodeLookAndFeelThemes.getBlueIceTheme()));
				case DARK_BLUE_GREEN -> UIManager.setLookAndFeel(new InfoNodeLookAndFeel(InfoNodeLookAndFeelThemes.getDarkBlueGreenTheme()));
				case GRAY -> UIManager.setLookAndFeel(new InfoNodeLookAndFeel(InfoNodeLookAndFeelThemes.getGrayTheme()));
				case HIFI -> UIManager.setLookAndFeel(HiFiLookAndFeel.class.getName());
				case ACRYL -> UIManager.setLookAndFeel(AcrylLookAndFeel.class.getName());
				case ALUMINIUM -> UIManager.setLookAndFeel(AluminiumLookAndFeel.class.getName());
			}
		} catch (final UnsupportedLookAndFeelException
		               | ClassNotFoundException
		               | InstantiationException
		               | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
