package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum AiBuffer {
	WESTRING_NONE("_","WESTRING_UE_AIBUFFER_NONE"), HALL("townhall","WESTRING_UE_AIBUFFER_HALL"), RESOURCE("resource","WESTRING_UE_AIBUFFER_RESOURCE"), FACTORY("factory","WESTRING_UE_AIBUFFER_FACTORY"), GENERAL("buffer","WESTRING_UE_AIBUFFER_GENERAL");
	private final String codeName;
	private final String dispName;
	AiBuffer(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static AiBuffer fromCodeName(final String name) {
		for(final AiBuffer cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("AiBuffer does not exist: " + name);
	}
}
