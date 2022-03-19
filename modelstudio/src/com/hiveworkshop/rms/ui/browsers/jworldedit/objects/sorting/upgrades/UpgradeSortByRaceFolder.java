package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.upgrades;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObjectSortStringComparator;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortByRaceFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.abilities.DefaultAbilityRace;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.buffs.DefaultBuffRace;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.BottomLevelCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.SortRace;
import com.hiveworkshop.rms.util.War3ID;

import java.util.Arrays;
import java.util.List;

public final class UpgradeSortByRaceFolder extends SortByRaceFolder {

	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private static final War3ID UPGR_RACE_FIELD = War3ID.fromString("grac");

//	private final Map<String, SortingFolderTreeNode> raceFolders = new HashMap<>();
//	private final List<SortingFolderTreeNode> raceNodes = new ArrayList<>();

	public UpgradeSortByRaceFolder(final String displayName) {
		this(displayName, Arrays.asList(DefaultBuffRace.values()));
	}

	public UpgradeSortByRaceFolder(String displayName, List<SortRace> races) {
		super(displayName, races);
//		for (SortRace race : races) {
//			BottomLevelCategoryFolder bottomLevelFolder = getFolder(race);
//			raceFolders.put(race.getKeyString(), bottomLevelFolder);
//			raceNodes.add(bottomLevelFolder);
//		}
	}

	protected BottomLevelCategoryFolder getFolder(SortRace race) {
		return new BottomLevelCategoryFolder(race.getDisplayName(), new MutableGameObjectSortStringComparator());
	}

	private DefaultAbilityRace raceKey(final int index) {
		return switch (index) {
			case -1, 0 -> DefaultAbilityRace.HUMAN;
			case 1 -> DefaultAbilityRace.ORC;
			case 2 -> DefaultAbilityRace.UNDEAD;
			case 3 -> DefaultAbilityRace.NIGHTELF;
			case 4 -> DefaultAbilityRace.OTHER;
			case 5 -> DefaultAbilityRace.NEUTRAL_HOSTILE;
			case 6 -> DefaultAbilityRace.NEUTRAL_PASSIVE;
			default -> DefaultAbilityRace.NEUTRAL_PASSIVE;
		};
	}

	protected War3ID getWar3ID() {
		return UPGR_RACE_FIELD;
	}

//	@Override
//	public int getSortIndex(final SortingFolderTreeNode childNode) {
////		return raceNodes.indexOf(childNode);
//		if (childNode != null){
//			return raceNodes.indexOf(childNode);
//		}
//		return -1;
//	}
}
