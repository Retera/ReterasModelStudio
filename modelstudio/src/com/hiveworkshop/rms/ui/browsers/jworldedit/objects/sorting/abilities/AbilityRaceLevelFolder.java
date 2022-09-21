package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.abilities;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.BottomLevelCategoryFolder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.WE_Field;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.List;

public final class AbilityRaceLevelFolder extends AbstractSortingFolderTreeNode {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
//	private static final long serialVersionUID = 1L;
	private static final String TAG_NAME = "sort";

	private final BottomLevelCategoryFolder units;
	private final BottomLevelCategoryFolder heroes;
	private final BottomLevelCategoryFolder items;
	private final BottomLevelCategoryFolder hidden;
	private final List<BottomLevelCategoryFolder> folders = new ArrayList<>();

	public AbilityRaceLevelFolder(String displayName) {
		super(displayName);
		this.units = new BottomLevelCategoryFolder(WEString.getString("WESTRING_UNITS"), this::compare);
		this.heroes = new BottomLevelCategoryFolder(WEString.getString("WESTRING_UTYPE_HEROES"), this::compare);
		this.items = new BottomLevelCategoryFolder(WEString.getString("WESTRING_AE_ITEMS"), this::compare);
		this.hidden = new BottomLevelCategoryFolder(WEString.getString("WESTRING_ITEMSTATUS_HIDDEN"), this::compare);
		folders.add(units);
		folders.add(heroes);
		folders.add(items);
		folders.add(hidden);
	}

	public SortingFolderTreeNode getNextNode(MutableGameObject object) {
		if (object.getFieldAsBoolean(WE_Field.ABIL_IS_HERO_ABIL.getId(), 0)) {
			return heroes;
		} else if (object.getFieldAsBoolean(WE_Field.ABIL_IS_ITEM_ABIL.getId(), 0)) {
			return items;
		} else if (!object.readSLKTagBoolean("useInEditor")) {
			return hidden;
		} else {
			return units;
		}
	}

	//	@Override
	public int getSortIndex(SortingFolderTreeNode childNode) {
		if (childNode != null) {
			return folders.indexOf(childNode);
		}
		return -1;
	}

	@Override
	public int getSortIndex(TreeNode childNode) {
		if (childNode != null) {
			return folders.indexOf(childNode);
		}
		return -1;
	}


	public int compare(final MutableGameObject a, final MutableGameObject b) {
		String a_slkTag = a.readSLKTag(TAG_NAME);
		String b_slkTag = b.readSLKTag(TAG_NAME);
		if (a_slkTag.equals("") && !b_slkTag.equals("")) {
			return 1;
		} else if (b_slkTag.equals("") && !a_slkTag.equals("")) {
			return -1;
		}
		final int comp1 = a_slkTag.compareTo(b_slkTag);
		if (comp1 == 0) {
			return a.getName().compareTo(b.getName());
		}
		return comp1;
	}
}
