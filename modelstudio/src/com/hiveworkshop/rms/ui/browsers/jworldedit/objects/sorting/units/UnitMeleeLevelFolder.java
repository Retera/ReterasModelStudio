package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.units;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.BottomLevelCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameUnitComparator;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.util.War3ID;

public final class UnitMeleeLevelFolder extends AbstractSortingFolderTreeNode {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private static final War3ID UNIT_IS_BUILDING_FIELD = War3ID.fromString("ubdg");
	private static final War3ID UNIT_CATEGORIZE_SPECIAL_FIELD = War3ID.fromString("uspe");
	private static final MutableGameUnitComparator MUTABLE_GAME_UNIT_COMPARATOR = new MutableGameUnitComparator();

	private final BottomLevelCategoryFolder units;
	private final BottomLevelCategoryFolder heroes;
	private final BottomLevelCategoryFolder buildings;
	private final BottomLevelCategoryFolder special;
	private final List<BottomLevelCategoryFolder> folders = new ArrayList<>();

	public UnitMeleeLevelFolder(final String displayName) {
		super(displayName);
		this.units = new BottomLevelCategoryFolder(WEString.getString("WESTRING_UNITS"), MUTABLE_GAME_UNIT_COMPARATOR);
		this.heroes = new BottomLevelCategoryFolder(WEString.getString("WESTRING_UTYPE_HEROES"),
				MUTABLE_GAME_UNIT_COMPARATOR);
		this.buildings = new BottomLevelCategoryFolder(WEString.getString("WESTRING_UTYPE_BUILDINGS"),
				MUTABLE_GAME_UNIT_COMPARATOR);
		this.special = new BottomLevelCategoryFolder(WEString.getString("WESTRING_UTYPE_SPECIAL"),
				MUTABLE_GAME_UNIT_COMPARATOR);
		folders.add(units);
		folders.add(buildings);
		folders.add(heroes);
		folders.add(special);
	}

	@Override
	public SortingFolderTreeNode getNextNode(final MutableGameObject object) {
		if (object.getFieldAsBoolean(UNIT_CATEGORIZE_SPECIAL_FIELD, 0)) {
			return special;
		} else if (object.getAlias().toString().length() > 1 && Character.isUpperCase(object.getAlias().charAt(0))) {
			return heroes;
		} else if (object.getFieldAsBoolean(UNIT_IS_BUILDING_FIELD, 0)) {
			return buildings;
		} else {
			return units;
		}
	}

	@Override
	public int getSortIndex(final DefaultMutableTreeNode childNode) {
		return folders.indexOf(childNode);
	}

}
