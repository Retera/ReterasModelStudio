package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import com.hiveworkshop.blizzard.casc.io.WC3CascFileSystem;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class LocaleChooser {

	public static String getLocale(Component parent,
	                               String launcherDbLocale,
	                               String originalInstallLocale,
	                               Map<String, Boolean> localeValidMap) {
		// gather list of locales from CASC
		JPanel chooseLocPanel = new JPanel();
		chooseLocPanel.setLayout(new MigLayout("fill, wrap 1"));

		chooseLocPanel.add(new JLabel("Originally installed locale: " + originalInstallLocale + ", Launcher.db locale: " + launcherDbLocale));
		chooseLocPanel.add(new JLabel("Locale could not be determined automatically. Please choose your locale."));
		chooseLocPanel.add(new JLabel("An incorrect choice may cause the Retera Model Studio to fail to start."));
		chooseLocPanel.add(new JLabel("Any option is valid if you have started the game using that locale at least once."));

		SmartButtonGroup buttonGroup = getLocaleGroup(localeValidMap);
		chooseLocPanel.add(buttonGroup.getButtonPanel());

		if (showConf(chooseLocPanel, "Choose Locale", parent) == JOptionPane.OK_OPTION) {
			int selectedIndex = buttonGroup.getSelectedIndex();
			if (selectedIndex == -1) {
				showMessage("User did not choose a locale! Aborting!", parent);
				return null;
			}
			return buttonGroup.getButton(selectedIndex).getText();
		}
		return null;
	}

	public static SmartButtonGroup getLocaleGroup(Map<String, Boolean> localeValidMap) {
		SmartButtonGroup localeGroup = new SmartButtonGroup();
		for (String localeOptionString : localeValidMap.keySet()) {
			JRadioButton radioButton = localeGroup.addJRadioButton(localeOptionString, null);
			if (!localeValidMap.get(localeOptionString)) {
				radioButton.setForeground(Color.RED.darker());
			}
		}
		if (localeGroup.getSelection() == null) {
			String firstValid = getFirstValid(localeValidMap);
			if (firstValid != null) {
				localeGroup.setSelectedName(firstValid);
			}
		}
		return localeGroup;
	}

	public static String getFirstValid(Map<String, Boolean> localeValidMap) {
		String firstValid = localeValidMap.keySet().stream().filter(localeValidMap::get).findFirst().orElse("enus");
		return localeValidMap.containsKey(firstValid) ? firstValid : null;
	}

	public static Map<String, Boolean> getLocaleOptions(WC3CascFileSystem rootFileSystem, SupportedCascPatchFormat patchFormat) {
		Set<String> localeOptions = new LinkedHashSet<>();
		try {
			if (rootFileSystem.isFile("index") && rootFileSystem.isFileAvailable("index")) {
				ByteBuffer buffer = rootFileSystem.readFileData("index");
				String[] lines = new String(buffer.array()).split("\\s+");
				for (String line : lines) {
					String[] splitLine = line.split("\\|");
					if (3 <= splitLine.length) {
						String category = splitLine[2];
						if (category.length() == 4) {
							localeOptions.add(category);
						}
					}
				}
			}
		} catch (IOException ignored) {}

		if (localeOptions.isEmpty()) {
			localeOptions.addAll(Arrays.asList("zhCN", "ruRU", "esES", "itIT", "zhTW", "frFR", "enUS", "koKR", "deDE", "plPL"));
		}

		Map<String, Boolean> isValidMap = new LinkedHashMap<>();
		for (String localeOptionString : localeOptions) {
			boolean isValid = isValidLocale(rootFileSystem, patchFormat, localeOptionString);
			isValidMap.put(localeOptionString, isValid);
		}
		return isValidMap;
	}

	public static boolean isValidLocale(WC3CascFileSystem rootFileSystem, SupportedCascPatchFormat patchFormat, String localeOptionString) {
		if (patchFormat != SupportedCascPatchFormat.UNKNOWN_FUTURE_PATCH) {
			String filePathToTest;
			if (patchFormat == SupportedCascPatchFormat.PATCH130) {
				filePathToTest = localeOptionString.toLowerCase() + "-war3local.mpq\\units\\campaignunitstrings.txt";
			} else {
				filePathToTest = "war3.w3mod\\_locales\\" + localeOptionString.toLowerCase() + ".w3mod\\units\\campaignunitstrings.txt";
			}
			try {
				return rootFileSystem.isFile(filePathToTest) && rootFileSystem.isFileAvailable(filePathToTest);
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}



	public static int showConf(JPanel messagePanel, String title, Component parent) {
		int option = JOptionPane.OK_CANCEL_OPTION;
		int type = JOptionPane.PLAIN_MESSAGE;
		return JOptionPane.showConfirmDialog(parent, messagePanel, title, option, type);
	}

	public static void showMessage(String message) {
		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void showMessage(String message, Component parent) {
		JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
