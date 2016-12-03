package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum RegenType {
	NONE("none","WESTRING_UE_REGENTYPE_NONE"), ALWAYS("always","WESTRING_UE_REGENTYPE_ALWAYS"), ONLY_WHILE_ON_BLIGHT("blight","WESTRING_UE_REGENTYPE_BLIGHT"), ONLY_DURING_THE_DAY("day","WESTRING_UE_REGENTYPE_DAY"), ONLY_DURING_THE_NIGHT("night","WESTRING_UE_REGENTYPE_NIGHT");
	private final String codeName;
	private final String dispName;
	RegenType(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static RegenType fromCodeName(final String name) {
		for(final RegenType cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("RegenType does not exist: " + name);
	}
}
