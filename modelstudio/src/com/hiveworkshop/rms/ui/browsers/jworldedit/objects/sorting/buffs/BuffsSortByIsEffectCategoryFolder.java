package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.buffs;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObjectSortStringComparator;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.BottomLevelCategoryFolder;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.tree.TreeNode;

public final class BuffsSortByIsEffectCategoryFolder extends AbstractSortingFolderTreeNode {
	private static final MutableGameObjectSortStringComparator MUTABLE_GAME_BUFF_COMPARATOR = new MutableGameObjectSortStringComparator();
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private static final War3ID IS_EFFECT_FIELD = War3ID.fromString("feff");
	private final BottomLevelCategoryFolder buff;
	private final BottomLevelCategoryFolder effect;

	public BuffsSortByIsEffectCategoryFolder(String displayName) {
		super(displayName);
		this.buff = new BottomLevelCategoryFolder(WEString.getString("WESTRING_BUFFTYPE_BUFF"), MUTABLE_GAME_BUFF_COMPARATOR);
		this.effect = new BottomLevelCategoryFolder(WEString.getString("WESTRING_BUFFTYPE_EFFECT"), MUTABLE_GAME_BUFF_COMPARATOR);
	}

	@Override
	public SortingFolderTreeNode getNextNode(MutableGameObject object) {
		boolean isEffect = object.getFieldAsBoolean(IS_EFFECT_FIELD, 0);
		return isEffect ? effect : buff;
	}

	//	@Override
	public int getSortIndex(SortingFolderTreeNode childNode) {
		return childNode == buff ? 0 : 1;
	}

	@Override
	public int getSortIndex(TreeNode childNode) {
		return childNode == buff ? 0 : 1;
	}
}
