package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.buffs;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.abilities.DefaultAbilityRace;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.SortRace;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

public final class BuffSortByRaceFolder extends AbstractSortingFolderTreeNode {

	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private static final War3ID BUFF_RACE_FIELD = War3ID.fromString("frac");

	private final Map<String, SortingFolderTreeNode> raceFolders;
	private final List<SortingFolderTreeNode> raceNodes;

	public BuffSortByRaceFolder(String displayName) {
		this(displayName, Arrays.asList(DefaultBuffRace.values()));
	}

	public BuffSortByRaceFolder(String displayName, List<SortRace> races) {
		super(displayName);
		raceFolders = new HashMap<>();
		raceNodes = new ArrayList<>();
		for (SortRace race : races) {
			BuffsSortByIsEffectCategoryFolder meleeCampaignFolder = new BuffsSortByIsEffectCategoryFolder(
					race.getDisplayName());
			raceFolders.put(race.getKeyString(), meleeCampaignFolder);
			raceNodes.add(meleeCampaignFolder);
		}
	}

	protected DefaultAbilityRace raceKey(int index) {
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

	@Override
	public SortingFolderTreeNode getNextNode(final MutableGameObject object) {
		String race = object.getFieldAsString(BUFF_RACE_FIELD, 0);
		DefaultAbilityRace raceKey = null;
		if ("naga".equals(race)) {
			race = "demon";
		}
		for (int i = 0; i < 6; i++) {
			if (race.equals(raceKey(i).getKeyString())) {
				raceKey = raceKey(i);
			}
		}
		if (raceKey == null) {
			if (raceFolders.containsKey(race)) {
				return raceFolders.get(race);
			} else {
				raceKey = DefaultAbilityRace.OTHER;
			}
		}
		return raceFolders.get(raceKey.getKeyString());
	}

	@Override
	public int getSortIndex(final DefaultMutableTreeNode childNode) {
		return raceNodes.indexOf(childNode);
	}
}
