package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum SilenceFlags {
	MELEE("0","WESTRING_UE_SILENCEFLAGS_MELEE"), RANGED("1","WESTRING_UE_SILENCEFLAGS_RANGED"), SPECIAL("2","WESTRING_UE_SILENCEFLAGS_SPECIAL"), SPELLS("3","WESTRING_UE_SILENCEFLAGS_SPELLS");
	private final String codeName;
	private final String dispName;
	SilenceFlags(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static SilenceFlags fromCodeName(final String name) {
		for(final SilenceFlags cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("SilenceFlags does not exist: " + name);
	}
}
