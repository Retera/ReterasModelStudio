package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum MoveType {
	FOOT("foot","WESTRING_UE_MOVETYPE_FOOT"), HORSE("horse","WESTRING_UE_MOVETYPE_HORSE"), FLY("fly","WESTRING_UE_MOVETYPE_FLY"), HOVER("hover","WESTRING_UE_MOVETYPE_HOVER"), FLOAT("float","WESTRING_UE_MOVETYPE_FLOAT"), AMPHIBIOUS("amph","WESTRING_UE_MOVETYPE_AMPH");
	private final String codeName;
	private final String dispName;
	MoveType(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static MoveType fromCodeName(final String name) {
		for(final MoveType cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("MoveType does not exist: " + name);
	}
}
