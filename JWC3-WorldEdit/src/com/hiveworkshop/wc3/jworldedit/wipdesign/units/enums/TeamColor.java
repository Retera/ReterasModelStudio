package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum TeamColor {
	MATCH_OWNING_PLAYER("-1","WESTRING_UE_TEAMCOLOR_NONE"), WESTRING_PLAYER_00("0","WESTRING_UE_TEAMCOLOR_00"), WESTRING_PLAYER_01("1","WESTRING_UE_TEAMCOLOR_01"), WESTRING_PLAYER_02("2","WESTRING_UE_TEAMCOLOR_02"), WESTRING_PLAYER_03("3","WESTRING_UE_TEAMCOLOR_03"), WESTRING_PLAYER_04("4","WESTRING_UE_TEAMCOLOR_04"), WESTRING_PLAYER_05("5","WESTRING_UE_TEAMCOLOR_05"), WESTRING_PLAYER_06("6","WESTRING_UE_TEAMCOLOR_06"), WESTRING_PLAYER_07("7","WESTRING_UE_TEAMCOLOR_07"), WESTRING_PLAYER_08("8","WESTRING_UE_TEAMCOLOR_08"), WESTRING_PLAYER_09("9","WESTRING_UE_TEAMCOLOR_09"), WESTRING_PLAYER_10("10","WESTRING_UE_TEAMCOLOR_10"), WESTRING_PLAYER_11("11","WESTRING_UE_TEAMCOLOR_11"), WESTRING_PLAYER_NA("12","WESTRING_UE_TEAMCOLOR_12");
	private final String codeName;
	private final String dispName;
	TeamColor(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static TeamColor fromCodeName(final String name) {
		for(final TeamColor cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("TeamColor does not exist: " + name);
	}
}
