package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general;

import javax.swing.tree.DefaultMutableTreeNode;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.TreeNodeLinker;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;

public final class TopLevelCategoryFolder extends AbstractSortingFolderTreeNode {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private final SortingFolderTreeNode standard;
	private final SortingFolderTreeNode custom;

	public TopLevelCategoryFolder(final SortingFolderTreeNode standard, final SortingFolderTreeNode custom) {
		this.standard = standard;
		this.custom = custom;
		add(standard);
		add(custom);
	}

	@Override
	public SortingFolderTreeNode getNextNode(final MutableGameObject object) {
		if (!object.isCustom()) {
			return standard;
		}
		return custom;
	}

	@Override
	public int getSortIndex(final DefaultMutableTreeNode childNode) {
		return childNode == standard ? 0 : 1;
	}

	public DefaultMutableTreeNode insertObjectInto(final MutableGameObject unit, final TreeNodeLinker linker) {
		SortingFolderTreeNode folderForUnit = null;
		SortingFolderTreeNode currentNode = this;
		while ((currentNode = currentNode.getNextNode(unit, linker)) != null) {
			folderForUnit = currentNode;
		}
		if (folderForUnit != null) {
			return folderForUnit.add(unit, linker);
		}
		return null;
	}

}
