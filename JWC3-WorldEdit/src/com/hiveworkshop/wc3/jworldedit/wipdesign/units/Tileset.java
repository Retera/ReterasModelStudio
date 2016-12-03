package com.hiveworkshop.wc3.jworldedit.wipdesign.units;

import com.hiveworkshop.wc3.resources.WEString;

public enum Tileset {
	ASHENVALE("A","WESTRING_LOCALE_ASHENVALE"), BARRENS("B","WESTRING_LOCALE_BARRENS"), BLACK_CITADEL("K","WESTRING_LOCALE_BLACKCITADEL"), CITYSCAPE("Y","WESTRING_LOCALE_CITYSCAPE"), DALARAN("X","WESTRING_LOCALE_DALARAN"), DALARAN_RUINS("J","WESTRING_LOCALE_DALARANRUINS"), DUNGEON("D","WESTRING_LOCALE_DUNGEON"), FELWOOD("C","WESTRING_LOCALE_FELWOOD"), ICECROWN_GLACIER("I","WESTRING_LOCALE_ICECROWN"), LORDAERON_FALL("F","WESTRING_LOCALE_LORDAERON_FALL"), LORDAERON_SUMMER("L","WESTRING_LOCALE_LORDAERON_SUMMER"), LORDAERON_WINTER("W","WESTRING_LOCALE_LORDAERON_WINTER"), NORTHREND("N","WESTRING_LOCALE_NORTHREND"), OUTLAND("O","WESTRING_LOCALE_OUTLAND"), SUNKEN_RUINS("Z","WESTRING_LOCALE_RUINS"), UNDERGROUND("G","WESTRING_LOCALE_DUNGEON2"), VILLAGE("V","WESTRING_LOCALE_VILLAGE"), VILLAGE_FALL("Q","WESTRING_LOCALE_VILLAGEFALL");
	private final String codeName;
	private final String dispName;
	Tileset(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static Tileset fromCodeName(final String name) {
		for(final Tileset cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("TileSets does not exist: " + name);
	}
}