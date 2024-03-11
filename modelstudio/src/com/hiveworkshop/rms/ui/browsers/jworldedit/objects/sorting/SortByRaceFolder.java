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
	protected final Map<String, SortingFolderTreeNode> raceFolders = new HashMap<>();
	protected final List<SortingFolderTreeNode> raceNodes = new ArrayList<>();

	public SortByRaceFolder(String displayName, List<SortRace> races) {
		super(displayName);
		for (SortRace race : races) {
			SortingFolderTreeNode folder = getFolder(race);
			raceFolders.put(race.getKeyString(), folder);
			raceNodes.add(folder);
		}
	}

	protected abstract SortingFolderTreeNode getFolder(SortRace race);

	@Override
	public SortingFolderTreeNode getNextNode(MutableGameObject object) {
		String race = object.getFieldAsString(getWar3ID(), 0);
		if ("naga".equals(race)) {
			race = "demon";
		}
		DefaultAbilityRace raceKey = DefaultAbilityRace.getFromKeyString(race);

		if (raceKey == null) {
			if (raceFolders.containsKey(race)) {
//				System.out.println("object " + object.getName() + " marked as " + race + " (" + raceFolders.get(race).toString() + ")");
				return raceFolders.get(race);
			} else {
//				System.out.println("object " + object.getName() + " marked as other");
				raceKey = DefaultAbilityRace.OTHER;
			}
		} else if (!raceFolders.containsKey(raceKey.getKeyString())){
			return raceFolders.get(DefaultAbilityRace.OTHER.getKeyString());
		}
		return raceFolders.get(raceKey.getKeyString());
	}

	protected abstract War3ID getWar3ID();

	//	@Override
	public int getSortIndex(SortingFolderTreeNode childNode) {
		if (childNode != null) {
			return raceNodes.indexOf(childNode);
		}
		return -1;
	}

	@Override
	public int getSortIndex(TreeNode childNode) {
		if (childNode instanceof SortingFolderTreeNode) {
			return raceNodes.indexOf(childNode);
		}
		return -1;
	}
}
