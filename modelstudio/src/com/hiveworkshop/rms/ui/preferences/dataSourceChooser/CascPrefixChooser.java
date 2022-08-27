package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import com.hiveworkshop.blizzard.casc.io.WC3CascFileSystem;
import com.hiveworkshop.blizzard.casc.io.WarcraftIIICASC;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class CascPrefixChooser {


	public static String getSpecificPrefix(Path installPathPath, Component parent) {
		// It's CASC. Now the question: what prefixes do we use?
		try {
			WarcraftIIICASC tempCascReader = new WarcraftIIICASC(installPathPath, true);
			DefaultComboBoxModel<String> prefixes = new DefaultComboBoxModel<>();
			try {
				WC3CascFileSystem rootFileSystem = tempCascReader.getRootFileSystem();
				List<String> allFiles = rootFileSystem.enumerateFiles();
				for (String file : allFiles) {
					if (rootFileSystem.isNestedFileSystem(file)) {
						prefixes.addElement(file);
					}
				}
			} finally {
				tempCascReader.close();
			}
			JComboBox<String> prefixChoiceComboBox = new JComboBox<>(prefixes);
			prefixChoiceComboBox.setEditable(true);

			JPanel comboBoxPanel = new JPanel(new BorderLayout());
			comboBoxPanel.add(prefixChoiceComboBox, BorderLayout.CENTER);
			comboBoxPanel.add(new JLabel("Choose a .w3mod:"), BorderLayout.BEFORE_FIRST_LINE);

			if (showConf(comboBoxPanel, "Choose Mod", parent) == JOptionPane.OK_OPTION) {
				Object selectedItem = prefixChoiceComboBox.getSelectedItem();
				if (selectedItem != null) {
					return selectedItem.toString();
				}
			}
		} catch (final Exception e1) {
			e1.printStackTrace();
			ExceptionPopup.display(e1);
		}
		return null;
	}
	public static List<String> getSpecificPrefixs(Path installPathPath, Component parent) {
		// It's CASC. Now the question: what prefixes do we use?
		try {
			WarcraftIIICASC tempCascReader = new WarcraftIIICASC(installPathPath, true);
			DefaultComboBoxModel<String> prefixes = new DefaultComboBoxModel<>();
			try {
				WC3CascFileSystem rootFileSystem = tempCascReader.getRootFileSystem();
				List<String> allFiles = rootFileSystem.enumerateFiles();
				for (String file : allFiles) {
					if (rootFileSystem.isNestedFileSystem(file)) {
						prefixes.addElement(file);
					}
				}
			} finally {
				tempCascReader.close();
			}
			JComboBox<String> prefixChoiceComboBox = new JComboBox<>(prefixes);
			prefixChoiceComboBox.setEditable(true);

			JPanel comboBoxPanel = new JPanel(new BorderLayout());
			comboBoxPanel.add(prefixChoiceComboBox, BorderLayout.CENTER);
			comboBoxPanel.add(new JLabel("Choose a .w3mod:"), BorderLayout.BEFORE_FIRST_LINE);

			if (showConf(comboBoxPanel, "Choose Mod", parent) == JOptionPane.OK_OPTION) {
				Object selectedItem = prefixChoiceComboBox.getSelectedItem();
				if (selectedItem != null) {
					return Collections.singletonList(selectedItem.toString());
				}
			}
		} catch (final Exception e1) {
			e1.printStackTrace();
			ExceptionPopup.display(e1);
		}
		return Collections.emptyList();
	}

	public static List<String> addDefaultCASCPrefixes(Path installPathPath, boolean allowPopup, Component parent) {
		// It's CASC. Now the question: what prefixes do we use?
		try {
			try (WarcraftIIICASC tempCascReader = new WarcraftIIICASC(installPathPath, true)) {
				String launcherDbLocale = getLauncherDbLocale(installPathPath);

				String originalInstallLocale = getOriginalInstallLocale(tempCascReader);

				WC3CascFileSystem rootFileSystem = tempCascReader.getRootFileSystem();
				SupportedCascPatchFormat patchFormat = getPatchFormat(tempCascReader, rootFileSystem);
				// Now, we really want to know the locale.
				String locale = LocaleChooser.getLocale(allowPopup, parent, launcherDbLocale, originalInstallLocale, tempCascReader, patchFormat, rootFileSystem);

				if (locale != null){
					if (patchFormat == SupportedCascPatchFormat.UNKNOWN_FUTURE_PATCH){
						showMessage(
								"The Warcraft III Installation you have selected seems to be too new, " +
										"or is not a supported version. The suggested prefix list from Patch 1.31 will be used." +
										"\nThis will probably fail, and you will need more advanced configuration.", parent);
					}
					return patchFormat.getPrefixes(locale);
				}
			}
		} catch (final Exception e1) {
			e1.printStackTrace();
			ExceptionPopup.display(e1);
		}
		return Collections.emptyList();
	}

	public static SupportedCascPatchFormat getPatchFormat(WarcraftIIICASC tempCascReader, WC3CascFileSystem rootFileSystem) throws IOException {
		SupportedCascPatchFormat patchFormat;
		if (rootFileSystem.isFile("war3.mpq\\units\\unitdata.slk")) {
			patchFormat = SupportedCascPatchFormat.PATCH130;
		} else if (tempCascReader.getRootFileSystem().isFile("war3.w3mod\\_hd.w3mod\\units\\human\\footman\\footman.mdx")) {
			patchFormat = SupportedCascPatchFormat.PATCH132;
		} else if (tempCascReader.getRootFileSystem().isFile("war3.w3mod\\units\\unitdata.slk")) {
			patchFormat = SupportedCascPatchFormat.PATCH131;
		} else {
			patchFormat = SupportedCascPatchFormat.UNKNOWN_FUTURE_PATCH;
		}
		return patchFormat;
	}

	public static String getOriginalInstallLocale(WarcraftIIICASC tempCascReader) {
		String originalInstallLocale = null;
		String tags = tempCascReader.getBuildInfo().getField(tempCascReader.getActiveRecordIndex(), "Tags");
		String[] splitTags = tags.split("\\?");
		for (String splitTag : splitTags) {
			String trimmedTag = splitTag.trim();
			int spaceIndex = trimmedTag.indexOf(' ');
			if (spaceIndex != -1) {
				String firstPart = trimmedTag.substring(0, spaceIndex);
				String secondPart = trimmedTag.substring(spaceIndex + 1);
				if (secondPart.equals("speech") || secondPart.equals("text")) {
					if (firstPart.length() == 4) {
						originalInstallLocale = firstPart;
					}
				}
			}
		}
		return originalInstallLocale;
	}

	public static String getLauncherDbLocale(Path installPathPath) {
		try {
			List<String> launcherDBLang = Files.readAllLines(installPathPath.resolve("Launcher.db"));
			if (launcherDBLang.size() > 0 && launcherDBLang.get(0).length() == 4) {
				return launcherDBLang.get(0);
			}
		} catch (final Exception ignored) {
		}
		return null;
	}
	public static int showConf(JPanel messagePanel, String title, Component parent) {
		int option = JOptionPane.OK_CANCEL_OPTION;
		int type = JOptionPane.PLAIN_MESSAGE;
		return JOptionPane.showConfirmDialog(parent, messagePanel, title, option, type);
	}

	public static void showMessage(String message, Component parent) {
		JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
