package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import com.hiveworkshop.blizzard.casc.io.WC3CascFileSystem;
import com.hiveworkshop.blizzard.casc.io.WarcraftIIICASC;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.*;

public class LocaleChooser {

	public static String getLocale(boolean allowPopup, String launcherDbLocale, String originalInstallLocale, WarcraftIIICASC tempCascReader,
	                         SupportedCascPatchFormat patchFormat, WC3CascFileSystem rootFileSystem) throws IOException {
		// gather list of locales from CASC
		Set<String> localeOptions = getLocaleOptions(rootFileSystem);
		JPanel chooseLocPanel = new JPanel();
		chooseLocPanel.setLayout(new MigLayout("fill, wrap 1"));

		chooseLocPanel.add(new JLabel("Originally installed locale: " + originalInstallLocale + ", Launcher.db locale: " + launcherDbLocale));
		chooseLocPanel.add(new JLabel("Locale could not be determined automatically. Please choose your locale."));
		chooseLocPanel.add(new JLabel("An incorrect choice may cause the Retera Model Studio to fail to start."));
		chooseLocPanel.add(new JLabel("Any option is valid if you have started the game using that locale at least once."));

		SmartButtonGroup buttonGroup = getLocaleGroup(tempCascReader, patchFormat, localeOptions);
		chooseLocPanel.add(buttonGroup.getButtonPanel());
		int confirmationResult = allowPopup ? showConf(chooseLocPanel, "Choose Locale") : JOptionPane.OK_OPTION;

		if (confirmationResult == JOptionPane.OK_OPTION) {
			int selectedIndex = buttonGroup.getSelectedIndex();
			if (selectedIndex == -1) {
				showMessage("User did not choose a locale! Aborting!");
				return null;
			}
			return buttonGroup.getButton(selectedIndex).getText();
		}
		return null;
	}

	public static List<JRadioButton> getRadioButtons(WarcraftIIICASC tempCascReader, SupportedCascPatchFormat patchFormat, Set<String> localeOptions) throws IOException {
		ButtonGroup buttonGroup = new ButtonGroup();
		List<JRadioButton> buttons = new ArrayList<>();
		boolean firstGoodButton = true;
		for (String localeOptionString : localeOptions) {
			JRadioButton radioButton = new JRadioButton(localeOptionString);
			boolean isValid = true;
			if(patchFormat != SupportedCascPatchFormat.UNKNOWN_FUTURE_PATCH){
				String filePathToTest;
				if (patchFormat != SupportedCascPatchFormat.PATCH130) {
					filePathToTest = localeOptionString.toLowerCase() + "-war3local.mpq\\units\\campaignunitstrings.txt";
				} else {
					filePathToTest = "war3.w3mod\\_locales\\" + localeOptionString.toLowerCase() + ".w3mod\\units\\campaignunitstrings.txt";
				}
				if (!tempCascReader.getRootFileSystem().isFile(filePathToTest) || !tempCascReader.getRootFileSystem().isFileAvailable(filePathToTest)) {
					radioButton.setForeground(Color.RED.darker());
					isValid = false;
				}
			}

			buttonGroup.add(radioButton);
			buttons.add(radioButton);
			if (isValid && (firstGoodButton || localeOptionString.equalsIgnoreCase("enus"))) {
				firstGoodButton = false;
				radioButton.setSelected(true);
			}
		}
		return buttons;
	}

	public static SmartButtonGroup getLocaleGroup(WarcraftIIICASC tempCascReader, SupportedCascPatchFormat patchFormat, Set<String> localeOptions) throws IOException {
		SmartButtonGroup localeGroup = new SmartButtonGroup();
		for (String localeOptionString : localeOptions) {
			JRadioButton radioButton = localeGroup.addJRadioButton(localeOptionString, null);
			boolean isValid = isValidLocale(tempCascReader, patchFormat, localeOptionString);
			if(!isValid){
				radioButton.setForeground(Color.RED.darker());
			} else if (localeGroup.getSelection() == null || localeOptionString.equalsIgnoreCase("enus")) {
				localeGroup.setSelected(radioButton.getModel(), true);
				radioButton.setSelected(true);
			}
		}
		return localeGroup;
	}

	public static boolean isValidLocale(WarcraftIIICASC tempCascReader, SupportedCascPatchFormat patchFormat, String localeOptionString) throws IOException {
		if(patchFormat != SupportedCascPatchFormat.UNKNOWN_FUTURE_PATCH){
			String filePathToTest;
			if (patchFormat == SupportedCascPatchFormat.PATCH130) {
				filePathToTest = localeOptionString.toLowerCase() + "-war3local.mpq\\units\\campaignunitstrings.txt";
			} else {
				filePathToTest = "war3.w3mod\\_locales\\" + localeOptionString.toLowerCase() + ".w3mod\\units\\campaignunitstrings.txt";
			}

			return !(!tempCascReader.getRootFileSystem().isFile(filePathToTest) || !tempCascReader.getRootFileSystem().isFileAvailable(filePathToTest));
		}
		return true;
	}

	public static Set<String> getLocaleOptions(WC3CascFileSystem rootFileSystem) throws IOException {
		Set<String> localeOptions = new HashSet<>();
		if (rootFileSystem.isFile("index") && rootFileSystem.isFileAvailable("index")) {
			ByteBuffer buffer = rootFileSystem.readFileData("index");
			Set<String> categories = new HashSet<>();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buffer.array())))) {
				String line;
				while ((line = reader.readLine()) != null) {
					String[] splitLine = line.split("\\|");
					if (splitLine.length >= 3) {
						String category = splitLine[2];
						categories.add(category);
					}
				}
			}
			for (String category : categories) {
				if (category.length() == 4) {
					localeOptions.add(category);
				}
			}
		}


		if (rootFileSystem.isFile("index") && rootFileSystem.isFileAvailable("index")) {
			ByteBuffer buffer = rootFileSystem.readFileData("index");
			String[] lines = new String(buffer.array()).split("\n");
			for(String line : lines){
				String[] splitLine = line.split("\\|");
				if (splitLine.length >= 3) {
					String category = splitLine[2];
					if (category.length() == 4) {
						localeOptions.add(category);
					}
				}
			}
		}
		if (localeOptions.isEmpty()) {
			localeOptions.addAll(Arrays.asList(
					"zhCN", "ruRU", "esES", "itIT", "zhTW", "frFR", "enUS", "koKR", "deDE", "plPL"));
		}
		return localeOptions;
	}



	public static int showConf(JPanel messagePanel, String title) {
		int option = JOptionPane.OK_CANCEL_OPTION;
		int type = JOptionPane.PLAIN_MESSAGE;
		return JOptionPane.showConfirmDialog(null, messagePanel, title, option, type);
	}

	public static void showMessage(String message) {
		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
