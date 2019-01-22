package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum ChannelFlags {
	VISIBLE("0","WESTRING_UE_CHANNELFLAGS_VISIBLE"), TARGETING_IMAGE("1","WESTRING_UE_CHANNELFLAGS_TARGIMAGE"), PHYSICAL_SPELL("2","WESTRING_UE_CHANNELFLAGS_PHYSICAL"), UNIVERSAL_SPELL("3","WESTRING_UE_CHANNELFLAGS_UNIVERSAL"), UNIQUE_CAST("4","WESTRING_UE_CHANNELFLAGS_UNIQUE");
	private final String codeName;
	private final String dispName;
	ChannelFlags(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static ChannelFlags fromCodeName(final String name) {
		for(final ChannelFlags cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("ChannelFlags does not exist: " + name);
	}
}
