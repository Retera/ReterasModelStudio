package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum UnitRace {
	WESTRING_RACE_HUMAN("human","WESTRING_UE_UNITRACE_HUMAN"), WESTRING_RACE_ORC("orc","WESTRING_UE_UNITRACE_ORC"), WESTRING_RACE_UNDEAD("undead","WESTRING_UE_UNITRACE_UNDEAD"), WESTRING_RACE_NIGHTELF("nightelf","WESTRING_UE_UNITRACE_NIGHTELF"), DEMON("demon","WESTRING_UE_UNITRACE_DEMON"), CREEP("creeps","WESTRING_UE_UNITRACE_CREEPS"), CRITTER("critters","WESTRING_UE_UNITRACE_CRITTERS"), OTHER("other","WESTRING_UE_UNITRACE_OTHER"), COMMONER("commoner","WESTRING_UE_UNITRACE_COMMONER"), NAGA("naga","WESTRING_UE_UNITRACE_NAGA"), NONE("unknown","WESTRING_UE_UNITRACE_UNKNOWN");
	private final String codeName;
	private final String dispName;
	UnitRace(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static UnitRace fromCodeName(final String name) {
		for(final UnitRace cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("UnitRace does not exist: " + name);
	}
}
