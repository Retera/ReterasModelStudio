package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.units;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortByRaceFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.SortRace;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.WE_Field;
import com.hiveworkshop.rms.util.War3ID;

import java.util.Arrays;
import java.util.List;

public final class UnitSortByRaceFolder extends SortByRaceFolder {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;

	private static final List<String> defaultNeutralRaces = Arrays.asList("commoner", "demon", "critters", "other", "creeps", "");

	public UnitSortByRaceFolder(String displayName) {
		this(displayName, Arrays.asList(DefaultUnitRace.values()));
	}

	public UnitSortByRaceFolder(String displayName, List<SortRace> races) {
		super(displayName, races);
	}

	protected UnitRaceLevelFolder getFolder(SortRace race) {
		return new UnitRaceLevelFolder(race);
	}

	@Override
	public SortingFolderTreeNode getNextNode(MutableGameObject object) {
		String race = object.getFieldAsString(getWar3ID(), 0);

		DefaultUnitRace raceKey = getRaceKey(object, race);
		if (raceKey != null) {
			return raceFolders.get(raceKey.getKeyString());
		} else if (raceFolders.containsKey(race)) {
			return raceFolders.get(race);
		} else {
			SortingFolderTreeNode sortingFolderTreeNode = getSortingFolderTreeNode(race);
			raceFolders.put(race, sortingFolderTreeNode);
			return sortingFolderTreeNode;
		}
	}

	private DefaultUnitRace getRaceKey(MutableGameObject object, String race) {
		DefaultUnitRace raceKey = DefaultUnitRace.getFromKeyString(race);

		if (raceKey == DefaultUnitRace.NEUTRAL_PASSIVE
				|| raceKey == null && defaultNeutralRaces.contains(race) ) {
			boolean isHostile = object.getFieldAsBoolean(WE_Field.UNIT_DISPLAY_AS_NEUTRAL_HOSTILE.getId(), 0);
			return isHostile ? DefaultUnitRace.NEUTRAL_HOSTILE : DefaultUnitRace.NEUTRAL_PASSIVE;
		}
		return raceKey;
	}

	private SortingFolderTreeNode getSortingFolderTreeNode(String race) {
		return new UnitRaceLevelFolder(new SortRace() {
			@Override
			public String getKeyString() {
				return race;
			}

			@Override
			public String getDisplayName() {
				return race;
			}
		});
	}

	protected War3ID getWar3ID() {
		return WE_Field.UNIT_RACE.getId();
	}
}
