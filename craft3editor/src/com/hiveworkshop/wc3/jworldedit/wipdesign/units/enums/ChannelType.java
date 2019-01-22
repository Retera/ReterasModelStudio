package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum ChannelType {
	INSTANT_NO_TARGET("0","WESTRING_UE_CHANNELTYPE_INSTANT"), UNIT_TARGET("1","WESTRING_UE_CHANNELTYPE_UNIT"), POINT_TARGET("2","WESTRING_UE_CHANNELTYPE_POINT"), UNIT_OR_POINT_TARGET("3","WESTRING_UE_CHANNELTYPE_UNITPOINT");
	private final String codeName;
	private final String dispName;
	ChannelType(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static ChannelType fromCodeName(final String name) {
		for(final ChannelType cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("ChannelType does not exist: " + name);
	}
}
