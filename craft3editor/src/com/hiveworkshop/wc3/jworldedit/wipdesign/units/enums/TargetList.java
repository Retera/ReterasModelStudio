package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum TargetList {
	AIR("air","WESTRING_UE_TARGETTYPE_AIR"), ALIVE("alive","WESTRING_UE_TARGETTYPE_ALIVE"), ALLIED("allies","WESTRING_UE_TARGETTYPE_ALLIES"), DEAD("dead","WESTRING_UE_TARGETTYPE_DEAD"), DEBRIS("debris","WESTRING_UE_TARGETTYPE_DEBRIS"), ENEMY("enemies","WESTRING_UE_TARGETTYPE_ENEMIES"), GROUND("ground","WESTRING_UE_TARGETTYPE_GROUND"), HERO("hero","WESTRING_UE_TARGETTYPE_HERO"), INVULNERABLE("invulnerable","WESTRING_UE_TARGETTYPE_INVULNERABLE"), ITEM("item","WESTRING_UE_TARGETTYPE_ITEM"), MECHANICAL("mechanical","WESTRING_UE_TARGETTYPE_MECHANICAL"), NEUTRAL("neutral","WESTRING_UE_TARGETTYPE_NEUTRAL"), NONE("none","WESTRING_UE_TARGETTYPE_NONE"), NON_HERO("nonhero","WESTRING_UE_TARGETTYPE_NONHERO"), NON_SUICIDAL("nonsapper","WESTRING_UE_TARGETTYPE_NONSAPPER"), NOT_SELF("notself","WESTRING_UE_TARGETTYPE_NOTSELF"), ORGANIC("organic","WESTRING_UE_TARGETTYPE_ORGANIC"), PLAYER_UNITS("player","WESTRING_UE_TARGETTYPE_PLAYERUNITS"), SUICIDAL("sapper","WESTRING_UE_TARGETTYPE_SAPPER"), SELF("self","WESTRING_UE_TARGETTYPE_SELF"), STRUCTURE("structure","WESTRING_UE_TARGETTYPE_STRUCTURE"), TERRAIN("terrain","WESTRING_UE_TARGETTYPE_TERRAIN"), TREE("tree","WESTRING_UE_TARGETTYPE_TREE"), VULNERABLE("vulnerable","WESTRING_UE_TARGETTYPE_VULNERABLE"), WALL("wall","WESTRING_UE_TARGETTYPE_WALL"), WARD("ward","WESTRING_UE_TARGETTYPE_WARD"), ANCIENT("ancient","WESTRING_UE_TARGETTYPE_ANCIENT"), NON_ANCIENT("nonancient","WESTRING_UE_TARGETTYPE_NONANCIENT"), FRIEND("friend","WESTRING_UE_TARGETTYPE_FRIEND"), BRIDGE("bridge","WESTRING_UE_TARGETTYPE_BRIDGE"), DECORATION("decoration","WESTRING_UE_TARGETTYPE_DECORATION");
	private final String codeName;
	private final String dispName;
	TargetList(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static TargetList fromCodeName(final String name) {
		for(final TargetList cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("TargetList does not exist: " + name);
	}
}
