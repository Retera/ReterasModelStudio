package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum SpellDetail {
	LOW("0","WESTRING_UE_SPELLDETAIL_LOW"), MEDIUM("1","WESTRING_UE_SPELLDETAIL_MEDIUM"), HIGH("2","WESTRING_UE_SPELLDETAIL_HIGH");
	private final String codeName;
	private final String dispName;
	SpellDetail(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static SpellDetail fromCodeName(final String name) {
		for(final SpellDetail cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("SpellDetail does not exist: " + name);
	}
}
