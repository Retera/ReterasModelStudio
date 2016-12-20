package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum CombatSound {
	AXE_MEDIUM_CHOP("AxeMediumChop","WESTRING_UE_COMBATSOUND_AXEMEDIUMCHOP"), METAL_HEAVY_BASH("MetalHeavyBash","WESTRING_UE_COMBATSOUND_METALHEAVYBASH"), METAL_HEAVY_CHOP("MetalHeavyChop","WESTRING_UE_COMBATSOUND_METALHEAVYCHOP"), METAL_HEAVY_SLICE("MetalHeavySlice","WESTRING_UE_COMBATSOUND_METALHEAVYSLICE"), METAL_LIGHT_CHOP("MetalLightChop","WESTRING_UE_COMBATSOUND_METALLIGHTCHOP"), METAL_LIGHT_SLICE("MetalLightSlice","WESTRING_UE_COMBATSOUND_METALLIGHTSLICE"), METAL_MEDIUM_BASH("MetalMediumBash","WESTRING_UE_COMBATSOUND_METALMEDIUMBASH"), METAL_MEDIUM_CHOP("MetalMediumChop","WESTRING_UE_COMBATSOUND_METALMEDIUMCHOP"), METAL_MEDIUM_SLICE("MetalMediumSlice","WESTRING_UE_COMBATSOUND_METALMEDIUMSLICE"), ROCK_HEAVY_BASH("RockHeavyBash","WESTRING_UE_COMBATSOUND_ROCKHEAVYBASH"), WOOD_HEAVY_BASH("WoodHeavyBash","WESTRING_UE_COMBATSOUND_WOODHEAVYBASH"), WOOD_LIGHT_BASH("WoodLightBash","WESTRING_UE_COMBATSOUND_WOODLIGHTBASH"), WOOD_MEDIUM_BASH("WoodMediumBash","WESTRING_UE_COMBATSOUND_WOODMEDIUMBASH");
	private final String codeName;
	private final String dispName;
	CombatSound(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static CombatSound fromCodeName(final String name) {
		for(final CombatSound cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("CombatSound does not exist: " + name);
	}
}
