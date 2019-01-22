package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum FullFlags {
	NEVER("0","WESTRING_UE_FULLFLAGS_NONE"), LIFE_ONLY("1","WESTRING_UE_FULLFLAGS_LIFE"), MANA_ONLY("2","WESTRING_UE_FULLFLAGS_MANA"), ALWAYS("3","WESTRING_UE_FULLFLAGS_BOTH");
	private final String codeName;
	private final String dispName;
	FullFlags(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static FullFlags fromCodeName(final String name) {
		for(final FullFlags cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("FullFlags does not exist: " + name);
	}
}
