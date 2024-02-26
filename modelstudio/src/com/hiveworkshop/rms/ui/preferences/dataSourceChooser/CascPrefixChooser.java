package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import com.hiveworkshop.blizzard.casc.io.WC3CascFileSystem;
import com.hiveworkshop.blizzard.casc.io.WarcraftIIICASC;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.TwiComboBox;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CascPrefixChooser {

	public static List<String> getSpecificPrefix(String installPath, Component parent) {
		// It's CASC. Now the question: what prefixes do we use?
		List<String> allCascPrefixes = getAllCascPrefixes(Paths.get(installPath));

		TwiComboBox<String> prefixChoiceComboBox = new TwiComboBox<>(allCascPrefixes, "a prototype value");
		prefixChoiceComboBox.setEditable(true);

		JPanel comboBoxPanel = new JPanel(new BorderLayout());
		comboBoxPanel.add(prefixChoiceComboBox, BorderLayout.CENTER);
		comboBoxPanel.add(new JLabel("Choose a .w3mod:"), BorderLayout.BEFORE_FIRST_LINE);

		if (showConf(comboBoxPanel, "Choose Mod", parent) == JOptionPane.OK_OPTION) {
			String selectedItem = prefixChoiceComboBox.getSelected();
			if (selectedItem != null) {
				return Collections.singletonList(selectedItem);
			}
		}

		return Collections.emptyList();
	}

	private static List<String> getAllCascPrefixes(Path installPath) {
		List<String> prefixes = new ArrayList<>();
		try (WarcraftIIICASC tempCascReader = new WarcraftIIICASC(installPath, true)) {
			WC3CascFileSystem rootFileSystem = tempCascReader.getRootFileSystem();
			List<String> allFiles = rootFileSystem.enumerateFiles();
			for (String file : allFiles) {
				if (rootFileSystem.isNestedFileSystem(file)) {
					prefixes.add(file);
				}
			}
		} catch (IOException ignored) {}
		return prefixes;
	}

	public static List<String> getDefaultCASCPrefixes(String installPath, boolean askLocale, Component parent) {
		CascInstallInfo cascInstallInfo = new CascInstallInfo(installPath);
		String locale;
		if (askLocale) {
			locale = LocaleChooser.getLocale(parent, cascInstallInfo.launcherDbLocale, cascInstallInfo.originalInstallLocale, cascInstallInfo.localeValidMap);
		} else {
			locale = LocaleChooser.getFirstValid(cascInstallInfo.localeValidMap);
		}
		if (locale != null) {
			SupportedCascPatchFormat validPatch = getValidPatch(cascInstallInfo.patchFormat, SupportedCascPatchFormat.PATCH131, askLocale, parent);
			return validPatch.getPrefixes(locale);
		}
		return null;
	}

	private static SupportedCascPatchFormat getValidPatch(SupportedCascPatchFormat patchFormat, SupportedCascPatchFormat fallback, boolean userChosen, Component parent) {
		if (patchFormat == SupportedCascPatchFormat.UNKNOWN_FUTURE_PATCH) {
			String locateType = userChosen ? "selected" : "found";
			showMessage(
					"The Warcraft III Installation " + locateType + " seems to be too new, or is not a supported version."
							+ "\nThe suggested prefix list from Patch " + fallback.display + " will be used."
							+ "\nThis will probably fail, and you will need more advanced configuration.", parent);
			return fallback;
		}
		return patchFormat;
	}

	public static SupportedCascPatchFormat getPatchFormat(WC3CascFileSystem rootFileSystem) throws IOException {
		SupportedCascPatchFormat patchFormat;
		if (rootFileSystem.isFile("war3.mpq\\units\\unitdata.slk")) {
			patchFormat = SupportedCascPatchFormat.PATCH130;
		} else if (rootFileSystem.isFile("war3.w3mod\\_hd.w3mod\\units\\human\\footman\\footman.mdx")) {
			patchFormat = SupportedCascPatchFormat.PATCH132;
		} else if (rootFileSystem.isFile("war3.w3mod\\units\\unitdata.slk")) {
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
		} catch (final Exception ignored) {}
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

	private static class CascInstallInfo {
		private SupportedCascPatchFormat patchFormat;
		private String launcherDbLocale;
		private String originalInstallLocale;
		private Map<String, Boolean> localeValidMap;
		CascInstallInfo(String installPath) {
			Path installPathPath = Paths.get(installPath);
			try (WarcraftIIICASC tempCascReader = new WarcraftIIICASC(installPathPath, true)) {
				WC3CascFileSystem rootFileSystem = tempCascReader.getRootFileSystem();
				patchFormat = getPatchFormat(rootFileSystem);
				launcherDbLocale = getLauncherDbLocale(installPathPath);
				originalInstallLocale = getOriginalInstallLocale(tempCascReader);
				localeValidMap = LocaleChooser.getLocaleOptions(rootFileSystem, patchFormat);
			} catch (final Exception e1) {
				e1.printStackTrace();
				ExceptionPopup.display(e1);
			}
		}
	}
}
