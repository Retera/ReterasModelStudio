package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.abilities;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortByRaceFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.SortRace;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.WE_Field;
import com.hiveworkshop.rms.util.War3ID;

import java.util.Arrays;
import java.util.List;

public final class AbilitySortByRaceFolder extends SortByRaceFolder {
	public AbilitySortByRaceFolder(String displayName) {
		this(displayName, Arrays.asList(DefaultAbilityRace.values()));
	}

	public AbilitySortByRaceFolder(String displayName, List<SortRace> races) {
		super(displayName, races);
	}

	protected AbilityRaceLevelFolder getFolder(SortRace race) {
		return new AbilityRaceLevelFolder(race.getDisplayName());
	}

	protected War3ID getWar3ID() {
		return WE_Field.ABIL_RACE.getId();
	}

}
