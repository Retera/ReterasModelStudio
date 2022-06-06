package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.units;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.BottomLevelCategoryFolder;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.List;

public final class UnitMeleeLevelFolder extends AbstractSortingFolderTreeNode {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private static final War3ID UNIT_IS_BUILDING_FIELD = War3ID.fromString("ubdg");
	private static final War3ID UNIT_CATEGORIZE_SPECIAL_FIELD = War3ID.fromString("uspe");
	private static final War3ID UNIT_LEVEL = War3ID.fromString("ulev");
	private static final String TAG_NAME = "unitClass";

	private final BottomLevelCategoryFolder units;
	private final BottomLevelCategoryFolder heroes;
	private final BottomLevelCategoryFolder buildings;
	private final BottomLevelCategoryFolder special;
	private final List<BottomLevelCategoryFolder> folders = new ArrayList<>();

	public UnitMeleeLevelFolder(String displayName) {
		super(displayName);
		this.units = new BottomLevelCategoryFolder(WEString.getString("WESTRING_UNITS"), this::compare);
		this.heroes = new BottomLevelCategoryFolder(WEString.getString("WESTRING_UTYPE_HEROES"), this::compare);
		this.buildings = new BottomLevelCategoryFolder(WEString.getString("WESTRING_UTYPE_BUILDINGS"), this::compare);
		this.special = new BottomLevelCategoryFolder(WEString.getString("WESTRING_UTYPE_SPECIAL"), this::compare);
		folders.add(units);
		folders.add(buildings);
		folders.add(heroes);
		folders.add(special);
	}

	@Override
	public SortingFolderTreeNode getNextNode(MutableGameObject object) {
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
			final int comp2 = Integer.compare(a.getFieldAsInteger(UNIT_LEVEL, 0), b.getFieldAsInteger(UNIT_LEVEL, 0));
			if (comp2 == 0) {
				return a.getName().compareTo(b.getName());
			}
			return comp2;
		}
		return comp1;
	}
}
