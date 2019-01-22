package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum DetectionType {
	NONE("0","WESTRING_UE_DETECTIONTYPE_NONE"), INVISIBLE("1","WESTRING_UE_DETECTIONTYPE_INVIS"), BURROWED("2","WESTRING_UE_DETECTIONTYPE_BURROW"), BOTH("3","WESTRING_UE_DETECTIONTYPE_BOTH");
	private final String codeName;
	private final String dispName;
	DetectionType(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static DetectionType fromCodeName(final String name) {
		for(final DetectionType cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("DetectionType does not exist: " + name);
	}
}
