package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.buffs;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.SortRace;

public enum DefaultBuffRace implements SortRace {
	HUMAN("human", "WESTRING_RACE_HUMAN"),
	ORC("orc", "WESTRING_RACE_ORC"),
	UNDEAD("undead", "WESTRING_RACE_UNDEAD"),
	NIGHTELF("nightelf", "WESTRING_RACE_NIGHTELF"),
	OTHER("other", "WESTRING_RACE_OTHER");

	private final String keyString;
	private final String displayName;

	DefaultBuffRace(final String keyString, final String displayKey) {
		this.keyString = keyString;
		displayName = WEString.getString(displayKey);
	}

	@Override
	public String getKeyString() {
		return keyString;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	public static DefaultBuffRace getFromKeyString(String string) {
		for (DefaultBuffRace abilityRace : DefaultBuffRace.values()) {
			if (string.equals(abilityRace.getKeyString())) {
				return abilityRace;
			}
		}
		return null;
	}
}