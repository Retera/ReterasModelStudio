package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum TechAvail {
	AVAILABLE("-1","WESTRING_UE_TECHAVAIL_AVAILABLE"), UNAVAILABLE("1","WESTRING_UE_TECHAVAIL_UNAVAILABLE");
	private final String codeName;
	private final String dispName;
	TechAvail(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static TechAvail fromCodeName(final String name) {
		for(final TechAvail cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("TechAvail does not exist: " + name);
	}
}
