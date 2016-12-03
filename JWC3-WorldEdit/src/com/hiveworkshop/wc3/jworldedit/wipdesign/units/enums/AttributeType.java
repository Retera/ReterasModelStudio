package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum AttributeType {
	AGILITY("AGI","WESTRING_UE_ATTRIBUTETYPE_AGI"), INTELLIGENCE("INT","WESTRING_UE_ATTRIBUTETYPE_INT"), STRENGTH("STR","WESTRING_UE_ATTRIBUTETYPE_STR");
	private final String codeName;
	private final String dispName;
	AttributeType(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static AttributeType fromCodeName(final String name) {
		for(final AttributeType cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("AttributeType does not exist: " + name);
	}
}
