package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum PickFlags {
	HALL("0","WESTRING_UE_PICKFLAGS_TOWNHALL"), RESOURCE("1","WESTRING_UE_PICKFLAGS_RESOURCE"), FACTORY("2","WESTRING_UE_PICKFLAGS_FACTORY"), GENERAL("3","WESTRING_UE_PICKFLAGS_BUFFER");
	private final String codeName;
	private final String dispName;
	PickFlags(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static PickFlags fromCodeName(final String name) {
		for(final PickFlags cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("PickFlags does not exist: " + name);
	}
}
