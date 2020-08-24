package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.units;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.SortRace;

public enum DefaultUnitRace implements SortRace {
	HUMAN("human", "WESTRING_RACE_HUMAN"), ORC("orc", "WESTRING_RACE_ORC"), UNDEAD("undead", "WESTRING_RACE_UNDEAD"),
	NIGHTELF("nightelf", "WESTRING_RACE_NIGHTELF"), NEUTRAL_NAGA("naga", "WESTRING_RACE_NEUTRAL_NAGA"),
	NEUTRAL_HOSTILE("hostiles", "WESTRING_NEUTRAL_HOSTILE"), NEUTRAL_PASSIVE("passive", "WESTRING_NEUTRAL_PASSIVE");

	private final String keyString;
	private final String displayName;

	DefaultUnitRace(final String keyString, final String displayKey) {
		this.keyString = keyString;
		displayName = displayKey;
	}

	@Override
	public String getKeyString() {
		return keyString;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
}