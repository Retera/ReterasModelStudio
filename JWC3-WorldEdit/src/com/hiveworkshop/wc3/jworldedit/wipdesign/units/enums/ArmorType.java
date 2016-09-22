package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum ArmorType {
	FLESH("Flesh","WESTRING_UE_ARMORTYPE_FLESH"), METAL("Metal","WESTRING_UE_ARMORTYPE_METAL"), WOOD("Wood","WESTRING_UE_ARMORTYPE_WOOD"), ETHEREAL("Ethereal","WESTRING_UE_ARMORTYPE_ETHEREAL"), STONE("Stone","WESTRING_UE_ARMORTYPE_STONE");
	private final String codeName;
	private final String dispName;
	ArmorType(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static ArmorType fromCodeName(final String name) {
		for(final ArmorType cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("ArmorType does not exist: " + name);
	}
}
