package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.TreeNodeLinker;

import javax.swing.tree.DefaultMutableTreeNode;

public final class TopLevelCategoryFolder extends AbstractSortingFolderTreeNode {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;
	private final SortingFolderTreeNode standard;
	private final SortingFolderTreeNode custom;

	public TopLevelCategoryFolder(SortingFolderTreeNode standard, SortingFolderTreeNode custom) {
		this.standard = standard;
		this.custom = custom;
		add(standard);
		add(custom);
	}

	@Override
	public SortingFolderTreeNode getNextNode(MutableGameObject object) {
		if (!object.isCustom()) {
			return standard;
		}
		return custom;
	}

	@Override
	public int getSortIndex(DefaultMutableTreeNode childNode) {
		return childNode == standard ? 0 : 1;
	}

	public DefaultMutableTreeNode insertObjectInto(MutableGameObject unit, TreeNodeLinker linker) {
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
