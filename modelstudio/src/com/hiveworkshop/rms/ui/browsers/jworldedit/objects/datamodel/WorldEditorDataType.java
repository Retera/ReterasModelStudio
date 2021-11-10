package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel;

public enum WorldEditorDataType {
	UNITS("w3u"),
	ITEM("w3t"),
	DESTRUCTIBLES("w3b"),
	DOODADS("w3d"),
	ABILITIES("w3a"),
	BUFFS_EFFECTS("w3h"),
	UPGRADES("w3q");

	private final String extension;

	WorldEditorDataType(final String extension) {
		this.extension = extension;
	}

	public String getExtension() {
		return extension;
	}
}
