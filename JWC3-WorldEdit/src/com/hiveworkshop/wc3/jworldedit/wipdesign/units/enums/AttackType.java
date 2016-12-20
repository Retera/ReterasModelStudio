package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum AttackType {
	NONE("unknown","WESTRING_NONE"), NORMAL("normal","WESTRING_UE_ATTACKTYPE_NORMAL"), PIERCE("pierce","WESTRING_UE_ATTACKTYPE_PIERCE"), SIEGE("siege","WESTRING_UE_ATTACKTYPE_SIEGE"), SPELLS("spells","WESTRING_UE_ATTACKTYPE_SPELLS"), CHAOS("chaos","WESTRING_UE_ATTACKTYPE_CHAOS"), MAGIC("magic","WESTRING_UE_ATTACKTYPE_MAGIC"), HERO("hero","WESTRING_UE_ATTACKTYPE_HERO");
	private final String codeName;
	private final String dispName;
	AttackType(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static AttackType fromCodeName(final String name) {
		for(final AttackType cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("AttackType does not exist: " + name);
	}
}
