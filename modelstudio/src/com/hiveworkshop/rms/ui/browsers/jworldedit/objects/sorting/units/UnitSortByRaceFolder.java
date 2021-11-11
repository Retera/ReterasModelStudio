package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.units;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortByRaceFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.SortRace;
import com.hiveworkshop.rms.util.War3ID;

import java.util.Arrays;
import java.util.List;

public final class UnitSortByRaceFolder extends SortByRaceFolder {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private static final War3ID UNIT_RACE_FIELD = War3ID.fromString("urac");
	private static final War3ID UNIT_DISPLAY_AS_NEUTRAL_HOSTILE_FIELD = War3ID.fromString("uhos");

	private static final List<String> defaultNeutralRaces = Arrays.asList("commoner", "demon", "critters", "other", "creeps", "");

//	private final Map<String, SortingFolderTreeNode> raceFolders = new HashMap<>();
//	private final List<SortingFolderTreeNode> raceNodes = new ArrayList<>();

	public UnitSortByRaceFolder(String displayName) {
		this(displayName, Arrays.asList(DefaultUnitRace.values()));
	}

	public UnitSortByRaceFolder(String displayName, List<SortRace> races) {
		super(displayName, races);
//		for (SortRace race : races) {
//			UnitRaceLevelFolder meleeCampaignFolder = getFolder(race);
//			raceFolders.put(race.getKeyString(), meleeCampaignFolder);
//			raceNodes.add(meleeCampaignFolder);
//		}
	}

	protected UnitRaceLevelFolder getFolder(SortRace race) {
		return new UnitRaceLevelFolder(race);
	}

	protected DefaultUnitRace raceKey(int index) {
		return switch (index) {
			case -1, 0 -> DefaultUnitRace.HUMAN;
			case 1 -> DefaultUnitRace.ORC;
			case 2 -> DefaultUnitRace.UNDEAD;
			case 3 -> DefaultUnitRace.NIGHTELF;
			case 4 -> DefaultUnitRace.NEUTRAL_NAGA;
			case 5 -> DefaultUnitRace.NEUTRAL_HOSTILE;
			case 6 -> DefaultUnitRace.NEUTRAL_PASSIVE;
			default -> DefaultUnitRace.NEUTRAL_PASSIVE;
		};
	}

	@Override
	public SortingFolderTreeNode getNextNode(MutableGameObject object) {
		String race = object.getFieldAsString(getWar3ID(), 0);

		DefaultUnitRace raceKey = DefaultUnitRace.getFromKeyString(race);

		if (raceKey == null && defaultNeutralRaces.contains(race)) {
			raceKey = DefaultUnitRace.NEUTRAL_PASSIVE;
		} else if (raceKey == null) {
			if (raceFolders.containsKey(race)) {
				return raceFolders.get(race);
			} else {
//				raceKey = DefaultUnitRace.NEUTRAL_PASSIVE;
				return getSortingFolderTreeNode(race);
			}
		}
		if ((raceKey == DefaultUnitRace.NEUTRAL_PASSIVE)
				&& object.getFieldAsBoolean(UNIT_DISPLAY_AS_NEUTRAL_HOSTILE_FIELD, 0)) {
			raceKey = DefaultUnitRace.NEUTRAL_HOSTILE;
		}
		return raceFolders.get(raceKey.getKeyString());
	}

	public SortingFolderTreeNode getNextNode2(MutableGameObject object) {
		String race = object.getFieldAsString(getWar3ID(), 0);

		DefaultUnitRace raceKey = DefaultUnitRace.getFromKeyString(race);

		if (raceKey == null && defaultNeutralRaces.contains(race)) {
			raceKey = DefaultUnitRace.NEUTRAL_PASSIVE;
		} else if (raceKey == null && raceFolders.containsKey(race)) {
			return raceFolders.get(race);
		} else if (raceKey == null) {
//				raceKey = DefaultUnitRace.NEUTRAL_PASSIVE;

			return getSortingFolderTreeNode(race);
		}
		if ((raceKey == DefaultUnitRace.NEUTRAL_PASSIVE)
				&& object.getFieldAsBoolean(UNIT_DISPLAY_AS_NEUTRAL_HOSTILE_FIELD, 0)) {
			raceKey = DefaultUnitRace.NEUTRAL_HOSTILE;
		}
		return raceFolders.get(raceKey.getKeyString());
	}

	private SortingFolderTreeNode getSortingFolderTreeNode(String race) {
		SortingFolderTreeNode sortingFolderTreeNode = raceFolders.get(race);
		if (sortingFolderTreeNode == null) {
			sortingFolderTreeNode = new UnitRaceLevelFolder(new SortRace() {
				@Override
				public String getKeyString() {
					return race;
				}

				@Override
				public String getDisplayName() {
					return race;
				}
			});
			raceFolders.put(race, sortingFolderTreeNode);
		}
		return sortingFolderTreeNode;
	}

	public SortingFolderTreeNode getNextNode1(MutableGameObject object) {
		String race = object.getFieldAsString(getWar3ID(), 0);

		DefaultUnitRace raceKey = DefaultUnitRace.getFromKeyString(race);

		if (raceKey == null) {
			if (defaultNeutralRaces.contains(race)) {
				raceKey = DefaultUnitRace.NEUTRAL_PASSIVE;
			} else if (raceFolders.containsKey(race)) {
				return raceFolders.get(race);
			} else {
//				raceKey = DefaultUnitRace.NEUTRAL_PASSIVE;

				SortingFolderTreeNode sortingFolderTreeNode = getSortingFolderTreeNode(race);
				return sortingFolderTreeNode;
			}
		}
		if ((raceKey == DefaultUnitRace.NEUTRAL_PASSIVE)
				&& object.getFieldAsBoolean(UNIT_DISPLAY_AS_NEUTRAL_HOSTILE_FIELD, 0)) {
			raceKey = DefaultUnitRace.NEUTRAL_HOSTILE;
		}
		return raceFolders.get(raceKey.getKeyString());
	}

	protected War3ID getWar3ID() {
		return UNIT_RACE_FIELD;
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
