package com.hiveworkshop.rms.ui.preferences;

public enum GUITheme {
	FOREST_GREEN("Forest Green"),
	DARK("Noire"),
	WINDOWS("Windows"),
	WINDOWS_CLASSIC("Windows Classic"),
	SOFT_GRAY("Soft Gray"),
	JAVA_DEFAULT("Java Default"),
	BLUE_ICE("Blue Ice"),
	DARK_BLUE_GREEN("Dark Blue-Green"),
	GRAY("Gray"),
	HIFI("HiFi"),
	ACRYL("Acryl"),
	ALUMINIUM("Aluminium");
	private final String displayName;

	GUITheme(final String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
//	0, 244,
//	1, 244,
//	2, 240,
//	3, 232,
//	4, 224,
//	5, 220,
//	6, 210,
//	7, 208,
//	8, 200,
//	9, 180,
//	10, 160,
//	11, 140,
//	12, 120,
//	13, 32,
//	,
//	TWI_ALUMINIUM("TwiAluminiumCopy")
}
