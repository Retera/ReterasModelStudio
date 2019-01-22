package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum DefenseTypeInt {
	NORMAL("4","WESTRING_UE_DEFENSETYPE_NORMAL"), SMALL("0","WESTRING_UE_DEFENSETYPE_SMALL"), MEDIUM("1","WESTRING_UE_DEFENSETYPE_MEDIUM"), LARGE("2","WESTRING_UE_DEFENSETYPE_LARGE"), FORTIFIED("3","WESTRING_UE_DEFENSETYPE_FORT"), HERO("5","WESTRING_UE_DEFENSETYPE_HERO"), DIVINE("6","WESTRING_UE_DEFENSETYPE_DIVINE"), UNARMORED("7","WESTRING_UE_DEFENSETYPE_NONE");
	private final String codeName;
	private final String dispName;
	DefenseTypeInt(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static DefenseTypeInt fromCodeName(final String name) {
		for(final DefenseTypeInt cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("DefenseTypeInt does not exist: " + name);
	}
}
