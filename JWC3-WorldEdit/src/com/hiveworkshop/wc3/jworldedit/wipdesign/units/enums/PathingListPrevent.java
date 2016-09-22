package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum PathingListPrevent {
	UNBLIGHTED("blighted","WESTRING_UE_PATHINGTYPE_PREVENT_BLIGHTED"), BUILDABLE("unbuildable","WESTRING_UE_PATHINGTYPE_PREVENT_UNBUILDABLE"), AIR_PATHABLE("unflyable","WESTRING_UE_PATHINGTYPE_PREVENT_UNFLYABLE"), GROUND_PATHABLE("unwalkable","WESTRING_UE_PATHINGTYPE_PREVENT_UNWALKABLE"), AMPHIBIOUS_PATHABLE("unamph","WESTRING_UE_PATHINGTYPE_PREVENT_UNAMPHIBIOUS"), SEA_PATHABLE("unfloat","WESTRING_UE_PATHINGTYPE_PREVENT_UNFLOATABLE");
	private final String codeName;
	private final String dispName;
	PathingListPrevent(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static PathingListPrevent fromCodeName(final String name) {
		for(final PathingListPrevent cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("PathingListPrevent does not exist: " + name);
	}
}
