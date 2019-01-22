package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum WeaponType {
	NORMAL("normal","WESTRING_UE_WEAPONTYPE_NORMAL"), INSTANT("instant","WESTRING_UE_WEAPONTYPE_INSTANT"), ARTILLERY("artillery","WESTRING_UE_WEAPONTYPE_ARTILLERY"), ARTILLERY_LINE("aline","WESTRING_UE_WEAPONTYPE_ALINE"), MISSILE("missile","WESTRING_UE_WEAPONTYPE_MISSILE"), MISSILE_SPLASH("msplash","WESTRING_UE_WEAPONTYPE_MSPLASH"), MISSILE_BOUNCE("mbounce","WESTRING_UE_WEAPONTYPE_MBOUNCE"), MISSILE_LINE("mline","WESTRING_UE_WEAPONTYPE_MLINE");
	private final String codeName;
	private final String dispName;
	WeaponType(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static WeaponType fromCodeName(final String name) {
		for(final WeaponType cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("WeaponType does not exist: " + name);
	}
}
