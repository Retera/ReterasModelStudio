package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum StackFlags {
	DAMAGE("0","WESTRING_UE_STACKFLAGS_DAMAGE"), MOVEMENT("1","WESTRING_UE_STACKFLAGS_MOVEMENT"), ATTACK_RATE("2","WESTRING_UE_STACKFLAGS_ATTACKRATE"), KILL_UNIT("3","WESTRING_UE_STACKFLAGS_KILLUNIT");
	private final String codeName;
	private final String dispName;
	StackFlags(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static StackFlags fromCodeName(final String name) {
		for(final StackFlags cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("StackFlags does not exist: " + name);
	}
}
