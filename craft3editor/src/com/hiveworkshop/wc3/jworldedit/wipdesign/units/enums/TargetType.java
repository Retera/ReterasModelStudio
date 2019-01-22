package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum TargetType {
	GROUND("ground","WESTRING_UE_TARGETTYPE_GROUND"), AIR("air","WESTRING_UE_TARGETTYPE_AIR"), STRUCTURE("structure","WESTRING_UE_TARGETTYPE_STRUCTURE"), WARD("ward","WESTRING_UE_TARGETTYPE_WARD");
	private final String codeName;
	private final String dispName;
	TargetType(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static TargetType fromCodeName(final String name) {
		for(final TargetType cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("TargetType does not exist: " + name);
	}
}
