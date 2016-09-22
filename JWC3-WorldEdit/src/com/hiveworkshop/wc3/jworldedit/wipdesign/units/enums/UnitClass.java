package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum UnitClass {
	GIANT("giant","WESTRING_UE_UNITCLASS_GIANT"), UNDEAD("undead","WESTRING_UE_UNITCLASS_UNDEAD"), SUMMONED("summoned","WESTRING_UE_UNITCLASS_SUMMONED"), MECHANICAL("mechanical","WESTRING_UE_UNITCLASS_MECHANICAL"), WORKER("peon","WESTRING_UE_UNITCLASS_PEON"), SUICIDAL("sapper","WESTRING_UE_UNITCLASS_SAPPER"), TOWN_HALL("townhall","WESTRING_UE_UNITCLASS_TOWNHALL"), TREE("tree","WESTRING_UE_UNITCLASS_TREE"), WARD("ward","WESTRING_UE_UNITCLASS_WARD"), ANCIENT("ancient","WESTRING_UE_UNITCLASS_ANCIENT"), WALKABLE("standon","WESTRING_UE_UNITCLASS_STANDON"), NEUTRAL("neutral","WESTRING_UE_UNITCLASS_NEUTRAL"), TAUREN("tauren","WESTRING_UE_UNITCLASS_TAUREN");
	private final String codeName;
	private final String dispName;
	UnitClass(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static UnitClass fromCodeName(final String name) {
		for(final UnitClass cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("UnitClass does not exist: " + name);
	}
}
