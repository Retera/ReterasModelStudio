package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.abilities.DefaultAbilityRace;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.SortRace;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SortByRaceFolder extends AbstractSortingFolderTreeNode {

	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private static final War3ID UPGR_RACE_FIELD = War3ID.fromString("grac");

	protected final Map<String, SortingFolderTreeNode> raceFolders = new HashMap<>();
	protected final List<SortingFolderTreeNode> raceNodes = new ArrayList<>();

//	public SortByRaceFolder(final String displayName) {
//		this(displayName, Arrays.asList(DefaultBuffRace.values()));
//	}

	public SortByRaceFolder(String displayName, List<SortRace> races) {
		super(displayName);
		for (SortRace race : races) {
			SortingFolderTreeNode folder = getFolder(race);
			raceFolders.put(race.getKeyString(), folder);
			raceNodes.add(folder);
		}
	}

	protected abstract SortingFolderTreeNode getFolder(SortRace race);

//	protected DefaultAbilityRace raceKey(final int index) {
//		return switch (index) {
//			case -1, 0 -> DefaultAbilityRace.HUMAN;
//			case 1 -> DefaultAbilityRace.ORC;
//			case 2 -> DefaultAbilityRace.UNDEAD;
//			case 3 -> DefaultAbilityRace.NIGHTELF;
//			case 4 -> DefaultAbilityRace.OTHER;
//			case 5 -> DefaultAbilityRace.NEUTRAL_HOSTILE;
//			case 6 -> DefaultAbilityRace.NEUTRAL_PASSIVE;
//			default -> DefaultAbilityRace.NEUTRAL_PASSIVE;
//		};
//	}

	@Override
	public SortingFolderTreeNode getNextNode(MutableGameObject object) {
		String race = object.getFieldAsString(getWar3ID(), 0);
		if ("naga".equals(race)) {
			race = "demon";
		}
		DefaultAbilityRace raceKey = DefaultAbilityRace.getFromKeyString(race);

		if (raceKey == null) {
			if (raceFolders.containsKey(race)) {
				return raceFolders.get(race);
			} else {
				raceKey = DefaultAbilityRace.OTHER;
			}
		}
		return raceFolders.get(raceKey.getKeyString());
	}

	protected abstract War3ID getWar3ID();

	//	@Override
	public int getSortIndex(SortingFolderTreeNode childNode) {
//		return raceNodes.indexOf(childNode);
		if (childNode != null) {
			return raceNodes.indexOf(childNode);
		}
		return -1;
	}

	@Override
	public int getSortIndex(TreeNode childNode) {
//		return raceNodes.indexOf(childNode);
		if (childNode != null) {
			return raceNodes.indexOf(childNode);
		}
		return -1;
	}
}
