package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.TreeNodeLinker;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public final class TopLevelCategoryFolder extends AbstractSortingFolderTreeNode {
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

	//	@Override
	public int getSortIndex(SortingFolderTreeNode childNode) {
		return childNode == standard ? 0 : 1;
	}

	@Override
	public int getSortIndex(TreeNode childNode) {
		return childNode == standard ? 0 : 1;
	}

	public DefaultMutableTreeNode insertObjectInto(MutableGameObject unit, TreeNodeLinker linker) {
		SortingFolderTreeNode folderForUnit = null;
		SortingFolderTreeNode currentNode = this.getNextNode(unit, linker);
		while (currentNode != null) {
			folderForUnit = currentNode;
			currentNode = currentNode.getNextNode(unit, linker);
		}
		if (folderForUnit != null) {
			return folderForUnit.add(unit, linker);
		}
		return null;
	}
	public DefaultMutableTreeNode insertObjectInto1(MutableGameObject unit, TreeNodeLinker linker) {
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
