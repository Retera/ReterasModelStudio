package com.hiveworkshop.wc3.jworldedit.objects.sorting.buffs;

import javax.swing.tree.DefaultMutableTreeNode;

import com.hiveworkshop.wc3.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.wc3.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.wc3.jworldedit.objects.sorting.general.BottomLevelCategoryFolder;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.MutableGameObjectSortStringComparator;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public final class BuffsSortByIsEffectCategoryFolder extends AbstractSortingFolderTreeNode {
	private static final MutableGameObjectSortStringComparator MUTABLE_GAME_BUFF_COMPARATOR = new MutableGameObjectSortStringComparator();
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private static final War3ID IS_EFFECT_FIELD = War3ID.fromString("feff");
	private final BottomLevelCategoryFolder buff;
	private final BottomLevelCategoryFolder effect;

	public BuffsSortByIsEffectCategoryFolder(final String displayName) {
		super(displayName);
		this.buff = new BottomLevelCategoryFolder(WEString.getString("WESTRING_BUFFTYPE_BUFF"), MUTABLE_GAME_BUFF_COMPARATOR);
		this.effect = new BottomLevelCategoryFolder(WEString.getString("WESTRING_BUFFTYPE_EFFECT"), MUTABLE_GAME_BUFF_COMPARATOR);
	}

	@Override
	public SortingFolderTreeNode getNextNode(final MutableGameObject object) {
		final boolean isEffect = object.getFieldAsBoolean(IS_EFFECT_FIELD, 0);
		return isEffect ? effect : buff;
	}

	@Override
	public int getSortIndex(final DefaultMutableTreeNode childNode) {
		return childNode == buff ? 0 : 1;
	}
}
