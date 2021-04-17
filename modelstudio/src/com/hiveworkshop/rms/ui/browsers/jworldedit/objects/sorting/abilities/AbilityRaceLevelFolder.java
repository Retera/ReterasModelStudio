package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.abilities;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.BottomLevelCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameAbilityComparator;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.util.War3ID;

public final class AbilityRaceLevelFolder extends AbstractSortingFolderTreeNode {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private static final War3ID ABIL_IS_ITEM_ABIL = War3ID.fromString("aite");
	private static final War3ID ABIL_IS_HERO_ABIL = War3ID.fromString("aher");
	private static final MutableGameAbilityComparator MUTABLE_GAME_ABIL_COMPARATOR = new MutableGameAbilityComparator();

	private final BottomLevelCategoryFolder units;
	private final BottomLevelCategoryFolder heroes;
	private final BottomLevelCategoryFolder items;
	private final BottomLevelCategoryFolder hidden;
	private final List<BottomLevelCategoryFolder> folders = new ArrayList<>();

	public AbilityRaceLevelFolder(final String displayName) {
		super(displayName);
		this.units = new BottomLevelCategoryFolder(WEString.getString("WESTRING_UNITS"), MUTABLE_GAME_ABIL_COMPARATOR);
		this.heroes = new BottomLevelCategoryFolder(WEString.getString("WESTRING_UTYPE_HEROES"),
				MUTABLE_GAME_ABIL_COMPARATOR);
		this.items = new BottomLevelCategoryFolder(WEString.getString("WESTRING_AE_ITEMS"),
				MUTABLE_GAME_ABIL_COMPARATOR);
		this.hidden = new BottomLevelCategoryFolder(WEString.getString("WESTRING_ITEMSTATUS_HIDDEN"),
				MUTABLE_GAME_ABIL_COMPARATOR);
		folders.add(units);
		folders.add(heroes);
		folders.add(items);
		folders.add(hidden);
	}

	@Override
	public SortingFolderTreeNode getNextNode(final MutableGameObject object) {
		if (object.getFieldAsBoolean(ABIL_IS_HERO_ABIL, 0)) {
			return heroes;
		} else if (object.getFieldAsBoolean(ABIL_IS_ITEM_ABIL, 0)) {
			return items;
		} else if (!object.readSLKTagBoolean("useInEditor")) {
			return hidden;
		} else {
			return units;
		}
	}

	@Override
	public int getSortIndex(final DefaultMutableTreeNode childNode) {
		return folders.indexOf(childNode);
	}

}
