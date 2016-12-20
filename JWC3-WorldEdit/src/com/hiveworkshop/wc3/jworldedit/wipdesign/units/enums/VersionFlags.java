package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum VersionFlags {
	REIGN_OF_CHAOS("0","WESTRING_UE_VERSIONFLAGS_VER0"), THE_FROZEN_THRONE("1","WESTRING_UE_VERSIONFLAGS_VER1");
	private final String codeName;
	private final String dispName;
	VersionFlags(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static VersionFlags fromCodeName(final String name) {
		for(final VersionFlags cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("VersionFlags does not exist: " + name);
	}
}
