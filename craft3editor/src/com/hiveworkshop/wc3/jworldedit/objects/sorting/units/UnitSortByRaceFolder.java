package com.hiveworkshop.wc3.jworldedit.objects.sorting.units;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import com.hiveworkshop.wc3.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.wc3.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.wc3.jworldedit.objects.sorting.general.SortRace;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public final class UnitSortByRaceFolder extends AbstractSortingFolderTreeNode {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private static final War3ID UNIT_RACE_FIELD = War3ID.fromString("urac");
	private static final War3ID UNIT_DISPLAY_AS_NEUTRAL_HOSTILE_FIELD = War3ID.fromString("uhos");

	private final Map<String, SortingFolderTreeNode> raceFolders;
	private final List<SortingFolderTreeNode> raceNodes;

	public UnitSortByRaceFolder(final String displayName) {
		this(displayName, Arrays.asList(DefaultUnitRace.values()));
	}

	public UnitSortByRaceFolder(final String displayName, final List<SortRace> races) {
		super(displayName);
		raceFolders = new HashMap<>();
		raceNodes = new ArrayList<>();
		for (final SortRace race : races) {
			final UnitRaceLevelFolder meleeCampaignFolder = new UnitRaceLevelFolder(race);
			raceFolders.put(race.getKeyString(), meleeCampaignFolder);
			raceNodes.add(meleeCampaignFolder);
		}
	}

	private DefaultUnitRace raceKey(final int index) {
		switch (index) {
		case -1:
			return DefaultUnitRace.HUMAN;
		case 0:
			return DefaultUnitRace.HUMAN;
		case 1:
			return DefaultUnitRace.ORC;
		case 2:
			return DefaultUnitRace.UNDEAD;
		case 3:
			return DefaultUnitRace.NIGHTELF;
		case 4:
			return DefaultUnitRace.NEUTRAL_NAGA;
		case 5:
			return DefaultUnitRace.NEUTRAL_HOSTILE;
		case 6:
			return DefaultUnitRace.NEUTRAL_PASSIVE;
		}
		return DefaultUnitRace.NEUTRAL_PASSIVE;
	}

	@Override
	public SortingFolderTreeNode getNextNode(final MutableGameObject object) {
		final String race = object.getFieldAsString(UNIT_RACE_FIELD, 0);
		DefaultUnitRace raceKey = null;
		for (int i = 0; i < 6; i++) {
			if (race.equals(raceKey(i).getKeyString())) {
				raceKey = raceKey(i);
			}
		}
		if (raceKey == null) {
			if (raceFolders.containsKey(race)) {
				return raceFolders.get(race);
			} else {
				raceKey = DefaultUnitRace.NEUTRAL_PASSIVE;
			}
		}
		if (raceKey == DefaultUnitRace.NEUTRAL_PASSIVE
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
