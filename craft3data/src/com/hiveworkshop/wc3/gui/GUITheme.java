package com.hiveworkshop.wc3.gui;
import hiveworkshop.localizationmanager.localizationmanager;

public enum GUITheme {
	FOREST_GREEN(LocalizationManager.getInstance().get("string.guitheme_guitheme_forest_green")),
	DARK(LocalizationManager.getInstance().get("string.guitheme_guitheme_noire")),
	WINDOWS(LocalizationManager.getInstance().get("string.guitheme_guitheme_windows")),
	WINDOWS_CLASSIC(LocalizationManager.getInstance().get("string.guitheme_guitheme_windows_lassic")),
	SOFT_GRAY(LocalizationManager.getInstance().get("string.guitheme_guitheme_soft_gray")),
	JAVA_DEFAULT(LocalizationManager.getInstance().get("string.guitheme_guitheme_java_default")),
	BLUE_ICE(LocalizationManager.getInstance().get("string.guitheme_guitheme_blue_ice")),
	DARK_BLUE_GREEN(LocalizationManager.getInstance().get("string.guitheme_guitheme_dark_blue_green")),
	GRAY(LocalizationManager.getInstance().get("string.guitheme_guitheme_gray")),
	HIFI(LocalizationManager.getInstance().get("string.guitheme_guitheme_hifi")),
	ACRYL(LocalizationManager.getInstance().get("string.guitheme_guitheme_acryl")),
	ALUMINIUM(LocalizationManager.getInstance().get("string.guitheme_guitheme_aluminium")),
	DEMONIC_MEME(LocalizationManager.getInstance().get("string.guitheme_guitheme_demonicmeme"));

	private String displayName;

	private GUITheme(final String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
