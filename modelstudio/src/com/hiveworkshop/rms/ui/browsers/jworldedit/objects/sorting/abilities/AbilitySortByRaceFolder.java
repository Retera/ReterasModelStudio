package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.abilities;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortByRaceFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.SortRace;
import com.hiveworkshop.rms.util.War3ID;

import java.util.Arrays;
import java.util.List;

public final class AbilitySortByRaceFolder extends SortByRaceFolder {

	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private static final War3ID ABIL_RACE_FIELD = War3ID.fromString("arac");

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
		return ABIL_RACE_FIELD;
	}

}
