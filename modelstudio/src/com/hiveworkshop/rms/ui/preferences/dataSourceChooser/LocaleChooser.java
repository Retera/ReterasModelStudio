package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import com.hiveworkshop.blizzard.casc.io.WC3CascFileSystem;
import com.hiveworkshop.blizzard.casc.io.WarcraftIIICASC;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class LocaleChooser {

	public static String getLocale(boolean allowPopup, Component parent,
	                               String launcherDbLocale,
	                               String originalInstallLocale,
	                               WarcraftIIICASC tempCascReader,
	                               SupportedCascPatchFormat patchFormat,
	                               WC3CascFileSystem rootFileSystem) throws IOException {
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
		int confirmationResult = allowPopup ? showConf(chooseLocPanel, "Choose Locale", parent) : JOptionPane.OK_OPTION;

		if (confirmationResult == JOptionPane.OK_OPTION) {
			int selectedIndex = buttonGroup.getSelectedIndex();
			if (selectedIndex == -1) {
				showMessage("User did not choose a locale! Aborting!", parent);
				return null;
			}
			return buttonGroup.getButton(selectedIndex).getText();
		}
		return null;
	}

	public static SmartButtonGroup getLocaleGroup(WarcraftIIICASC tempCascReader, SupportedCascPatchFormat patchFormat, Set<String> localeOptions) throws IOException {
		SmartButtonGroup localeGroup = new SmartButtonGroup();
		for (String localeOptionString : localeOptions) {
			JRadioButton radioButton = localeGroup.addJRadioButton(localeOptionString, null);
			boolean isValid = isValidLocale(tempCascReader, patchFormat, localeOptionString);
			if(!isValid){
				radioButton.setForeground(Color.RED.darker());
			} else if (localeGroup.getSelection() == null || localeOptionString.equalsIgnoreCase("enus")) {
				localeGroup.setSelectedName(localeOptionString);
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
			return tempCascReader.getRootFileSystem().isFile(filePathToTest) && tempCascReader.getRootFileSystem().isFileAvailable(filePathToTest);
		}
		return true;
	}

	public static Set<String> getLocaleOptions(WC3CascFileSystem rootFileSystem) throws IOException {
		Set<String> localeOptions = new LinkedHashSet<>();
		if (rootFileSystem.isFile("index") && rootFileSystem.isFileAvailable("index")) {
			ByteBuffer buffer = rootFileSystem.readFileData("index");
			String[] lines = new String(buffer.array()).split("\\s+");
			for(String line : lines){
				String[] splitLine = line.split("\\|");
				if (3 <= splitLine.length) {
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
