package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum MorphFlags {
	UNINTERRUPTABLE("0","WESTRING_UE_MORPHFLAGS_UNINTERRUPTABLE"), IMMEDIATE_LANDING("1","WESTRING_UE_MORPHFLAGS_IMMEDIATE_LANDING"), IMMEDIATE_TAKE_OFF("2","WESTRING_UE_MORPHFLAGS_IMMEDIATE_TAKEOFF"), PERMANENT("3","WESTRING_UE_MORPHFLAGS_PERMANENT"), REQUIRES_PAYMENT("4","WESTRING_UE_MORPHFLAGS_REQUIREPAYMENT");
	private final String codeName;
	private final String dispName;
	MorphFlags(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static MorphFlags fromCodeName(final String name) {
		for(final MorphFlags cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("MorphFlags does not exist: " + name);
	}
}
