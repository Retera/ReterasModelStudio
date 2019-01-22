package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum ShadowImage {
	NORMAL("Shadow","WESTRING_UE_SHADOWIMAGE_NORMAL"), FLYER("ShadowFlyer","WESTRING_UE_SHADOWIMAGE_FLYER");
	private final String codeName;
	private final String dispName;
	ShadowImage(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static ShadowImage fromCodeName(final String name) {
		for(final ShadowImage cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("ShadowImage does not exist: " + name);
	}
}
