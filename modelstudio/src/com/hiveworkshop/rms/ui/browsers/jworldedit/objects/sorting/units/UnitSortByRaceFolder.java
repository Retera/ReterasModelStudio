package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.units;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.SortRace;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

public final class UnitSortByRaceFolder extends AbstractSortingFolderTreeNode {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private static final War3ID UNIT_RACE_FIELD = War3ID.fromString("urac");
	private static final War3ID UNIT_DISPLAY_AS_NEUTRAL_HOSTILE_FIELD = War3ID.fromString("uhos");

	private final Map<String, SortingFolderTreeNode> raceFolders = new HashMap<>();
	private final List<SortingFolderTreeNode> raceNodes = new ArrayList<>();

	public UnitSortByRaceFolder(String displayName) {
		this(displayName, Arrays.asList(DefaultUnitRace.values()));
	}

	public UnitSortByRaceFolder(String displayName, List<SortRace> races) {
		super(displayName);
		for (SortRace race : races) {
			UnitRaceLevelFolder meleeCampaignFolder = new UnitRaceLevelFolder(race);
			raceFolders.put(race.getKeyString(), meleeCampaignFolder);
			raceNodes.add(meleeCampaignFolder);
		}
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

	private static final List<String> defaultNeutralRaces = Arrays.asList("commoner", "demon", "critters", "other",
			"creeps", "");

	@Override
	public SortingFolderTreeNode getNextNode(MutableGameObject object) {
		String race = object.getFieldAsString(UNIT_RACE_FIELD, 0);

		DefaultUnitRace raceKey = null;
		for (int i = 0; i < 6; i++) {
			if (race.equals(raceKey(i).getKeyString())) {
				raceKey = raceKey(i);
			}
		}
		if (raceKey == null) {
			if (defaultNeutralRaces.contains(race)) {
				raceKey = DefaultUnitRace.NEUTRAL_PASSIVE;
			} else if (raceFolders.containsKey(race)) {
				return raceFolders.get(race);
			} else {
//				raceKey = DefaultUnitRace.NEUTRAL_PASSIVE;

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
		}
		if ((raceKey == DefaultUnitRace.NEUTRAL_PASSIVE)
				&& object.getFieldAsBoolean(UNIT_DISPLAY_AS_NEUTRAL_HOSTILE_FIELD, 0)) {
			raceKey = DefaultUnitRace.NEUTRAL_HOSTILE;
		}
		final SortingFolderTreeNode sortingFolderTreeNode = raceFolders.get(raceKey.getKeyString());
		return sortingFolderTreeNode;
	}

	@Override
	public int getSortIndex(final DefaultMutableTreeNode childNode) {
		return raceNodes.indexOf(childNode);
	}
}
