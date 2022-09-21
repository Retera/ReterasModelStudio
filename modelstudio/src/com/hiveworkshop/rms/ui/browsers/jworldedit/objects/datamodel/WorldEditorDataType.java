package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel;

public enum WorldEditorDataType {
	UNITS("w3u", "U"),
	ITEM("w3t", "I"),
	DESTRUCTIBLES("w3b", "B"),
	DOODADS("w3d", "D"),
	ABILITIES("w3a", "A"),
	BUFFS_EFFECTS("w3h", "F"),
	UPGRADES("w3q", "G");
//	UNITS("w3u"),
//	ITEM("w3t"),
//	DESTRUCTIBLES("w3b"),
//	DOODADS("w3d"),
//	ABILITIES("w3a"),
//	BUFFS_EFFECTS("w3h"),
//	UPGRADES("w3q");

	private final String extension;
	private final String editString;

	//	WorldEditorDataType(final String extension) {
//		this.extension = extension;
//	}
	WorldEditorDataType(final String extension, final String editString) {
		this.extension = extension;
		this.editString = "WESTRING_" + editString + "E_DLG_EDITVALUE";
	}

	public String getExtension() {
		return extension;
	}

	public String getEditString() {
		return editString;
	}

//			case ABILITIES -> "WESTRING_AE_DLG_EDITVALUE";
//			case BUFFS_EFFECTS -> "WESTRING_FE_DLG_EDITVALUE";
//			case DESTRUCTIBLES -> "WESTRING_BE_DLG_EDITVALUE";
//			case DOODADS -> "WESTRING_DE_DLG_EDITVALUE";
//			case ITEM -> "WESTRING_IE_DLG_EDITVALUE";
//			case UPGRADES -> "WESTRING_GE_DLG_EDITVALUE";
//			case UNITS -> "WESTRING_UE_DLG_EDITVALUE";
}
