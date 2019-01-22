package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum ItemClass {
	PERMANENT("Permanent","WESTRING_ITEMCLASS_PERMANENT"), CHARGED("Charged","WESTRING_ITEMCLASS_CHARGED"), POWER_UP("PowerUp","WESTRING_ITEMCLASS_POWERUP"), ARTIFACT("Artifact","WESTRING_ITEMCLASS_ARTIFACT"), PURCHASABLE("Purchasable","WESTRING_ITEMCLASS_PURCHASABLE"), CAMPAIGN("Campaign","WESTRING_ITEMCLASS_CAMPAIGN"), MISCELLANEOUS("Miscellaneous","WESTRING_ITEMCLASS_MISCELLANEOUS");
	private final String codeName;
	private final String dispName;
	ItemClass(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static ItemClass fromCodeName(final String name) {
		for(final ItemClass cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("ItemClass does not exist: " + name);
	}
}
