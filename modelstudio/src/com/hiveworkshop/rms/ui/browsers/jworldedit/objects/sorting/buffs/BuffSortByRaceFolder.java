package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.buffs;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortByRaceFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.SortRace;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.WE_Field;
import com.hiveworkshop.rms.util.War3ID;

import java.util.Arrays;
import java.util.List;

public final class BuffSortByRaceFolder extends SortByRaceFolder {

	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;

	public BuffSortByRaceFolder(String displayName) {
		this(displayName, Arrays.asList(DefaultBuffRace.values()));
	}

	public BuffSortByRaceFolder(String displayName, List<SortRace> races) {
		super(displayName, races);
	}

	protected AbstractSortingFolderTreeNode getFolder(SortRace race) {
		return new BuffsSortByIsEffectCategoryFolder(race.getDisplayName());
	}

	protected War3ID getWar3ID() {
		return WE_Field.BUFF_RACE.getId();
	}
}
