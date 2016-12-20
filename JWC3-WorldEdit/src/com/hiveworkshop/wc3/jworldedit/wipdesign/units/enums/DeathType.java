package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum DeathType {
	CANT_RAISE_AND_DOES_NOT_DECAY("0","WESTRING_UE_DEATHTYPE_NONE"), CAN_RAISE_AND_DOES_NOT_DECAY("1","WESTRING_UE_DEATHTYPE_RAISE"), CANT_RAISE_AND_DOES_DECAY("2","WESTRING_UE_DEATHTYPE_DECAY"), CAN_RAISE_AND_DOES_DECAY("3","WESTRING_UE_DEATHTYPE_BOTH");
	private final String codeName;
	private final String dispName;
	DeathType(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static DeathType fromCodeName(final String name) {
		for(final DeathType cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("DeathType does not exist: " + name);
	}
}
