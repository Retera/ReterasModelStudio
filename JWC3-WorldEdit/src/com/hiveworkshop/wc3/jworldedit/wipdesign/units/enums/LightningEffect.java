package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum LightningEffect {
	CHAIN_LIGHTNING_PRIMARY("CLPB","WESTRING_UE_LIGHTNINGEFFECT_CLPB"), CHAIN_LIGHTNING_SECONDARY("CLSB","WESTRING_UE_LIGHTNINGEFFECT_CLSB"), MANA_BURN("MBUR","WESTRING_UE_LIGHTNINGEFFECT_MBUR"), LIGHTNING_ATTACK("CHIM","WESTRING_UE_LIGHTNINGEFFECT_CHIM"), FINGER_OF_DEATH("AFOD","WESTRING_UE_LIGHTNINGEFFECT_AFOD"), HEALING_WAVE_PRIMARY("HWPB","WESTRING_UE_LIGHTNINGEFFECT_HWPB"), HEALING_WAVE_SECONDARY("HWSB","WESTRING_UE_LIGHTNINGEFFECT_HWSB"), MANA_FLARE("MFPB","WESTRING_UE_LIGHTNINGEFFECT_MFPB"), DRAIN_LIFE_AND_MANA("DRAB","WESTRING_UE_LIGHTNINGEFFECT_DRAB"), DRAIN_LIFE("DRAL","WESTRING_UE_LIGHTNINGEFFECT_DRAL"), DRAIN_MANA("DRAM","WESTRING_UE_LIGHTNINGEFFECT_DRAM"), FORKED_LIGHTING("FORK","WESTRING_UE_LIGHTNINGEFFECT_FORK"), SPIRIT_LINK("SPLK","WESTRING_UE_LIGHTNINGEFFECT_SPLK"), AERIAL_SHACKLES("LEAS","WESTRING_UE_LIGHTNINGEFFECT_LEAS");
	private final String codeName;
	private final String dispName;
	LightningEffect(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static LightningEffect fromCodeName(final String name) {
		for(final LightningEffect cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("LightningEffect does not exist: " + name);
	}
}
