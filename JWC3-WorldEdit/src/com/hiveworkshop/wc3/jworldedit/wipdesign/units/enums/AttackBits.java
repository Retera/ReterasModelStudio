package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum AttackBits {
	NONE("0","WESTRING_UE_ATTACKBITS_NONE"), ATTACK_1_ONLY("1","WESTRING_UE_ATTACKBITS_ONE"), ATTACK_2_ONLY("2","WESTRING_UE_ATTACKBITS_TWO"), BOTH("3","WESTRING_UE_ATTACKBITS_BOTH");
	private final String codeName;
	private final String dispName;
	AttackBits(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static AttackBits fromCodeName(final String name) {
		for(final AttackBits cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("AttackBits does not exist: " + name);
	}
}
