package com.hiveworkshop.wc3.jworldedit.wipdesign.units.enums;

import com.hiveworkshop.wc3.resources.WEString;

public enum UpgradeClass {
	ARMOR("armor","WESTRING_GE_UPGRADECLASS_ARMOR"), ARTILLERY("artillery","WESTRING_GE_UPGRADECLASS_ARTILLERY"), MELEE("melee","WESTRING_GE_UPGRADECLASS_MELEE"), RANGED("ranged","WESTRING_GE_UPGRADECLASS_RANGED"), CASTER("caster","WESTRING_GE_UPGRADECLASS_CASTER");
	private final String codeName;
	private final String dispName;
	UpgradeClass(final String codeName, final String dispName) {
		this.codeName = codeName;
		this.dispName = dispName;
	}
	public final String getDisplayName() {
		return WEString.getString(dispName);
	}
	final public String getCodeName() {
		return codeName;
	}
	public static UpgradeClass fromCodeName(final String name) {
		for(final UpgradeClass cat: values()) {
			if( cat.getCodeName().equals(name) ) {
				return cat;
			}
		}
		throw new IllegalArgumentException("UpgradeClass does not exist: " + name);
	}
}
