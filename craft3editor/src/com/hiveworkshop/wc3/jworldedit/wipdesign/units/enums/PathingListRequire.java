package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum PathingListRequire {
	UNBLIGHTED("blighted","WESTRING_UE_PATHINGTYPE_REQUIRE_BLIGHTED"), BUILDABLE("unbuildable","WESTRING_UE_PATHINGTYPE_REQUIRE_UNBUILDABLE"), AIR_PATHABLE("unflyable","WESTRING_UE_PATHINGTYPE_REQUIRE_UNFLYABLE"), GROUND_PATHABLE("unwalkable","WESTRING_UE_PATHINGTYPE_REQUIRE_UNWALKABLE"), AMPHIBIOUS_PATHABLE("unamph","WESTRING_UE_PATHINGTYPE_REQUIRE_UNAMPHIBIOUS"), SEA_PATHABLE("unfloat","WESTRING_UE_PATHINGTYPE_REQUIRE_UNFLOATABLE");
	private final String codeName;
	private final String dispName;
	PathingListRequire(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static PathingListRequire fromCodeName(final String name) {
		for(final PathingListRequire cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("PathingListRequire does not exist: " + name);
	}
}
