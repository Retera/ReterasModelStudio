package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum DefenseType {
	NORMAL("normal","WESTRING_UE_DEFENSETYPE_NORMAL"), SMALL("small","WESTRING_UE_DEFENSETYPE_SMALL"), MEDIUM("medium","WESTRING_UE_DEFENSETYPE_MEDIUM"), LARGE("large","WESTRING_UE_DEFENSETYPE_LARGE"), FORTIFIED("fort","WESTRING_UE_DEFENSETYPE_FORT"), HERO("hero","WESTRING_UE_DEFENSETYPE_HERO"), DIVINE("divine","WESTRING_UE_DEFENSETYPE_DIVINE"), UNARMORED("none","WESTRING_UE_DEFENSETYPE_NONE");
	private final String codeName;
	private final String dispName;
	DefenseType(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static DefenseType fromCodeName(final String name) {
		for(final DefenseType cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("DefenseType does not exist: " + name);
	}
}
