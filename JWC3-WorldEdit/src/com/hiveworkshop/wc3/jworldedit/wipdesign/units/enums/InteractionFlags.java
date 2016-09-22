package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum InteractionFlags {
	ANY_UNIT_W_OR_INVENTORY("0","WESTRING_UE_INTERACTIONFLAGS_INVENTORY"), ANY_NON_BUILDING("1","WESTRING_UE_INTERACTIONFLAGS_UNIT"), ANY("2","WESTRING_UE_INTERACTIONFLAGS_ANY");
	private final String codeName;
	private final String dispName;
	InteractionFlags(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static InteractionFlags fromCodeName(final String name) {
		for(final InteractionFlags cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("InteractionFlags does not exist: " + name);
	}
}
