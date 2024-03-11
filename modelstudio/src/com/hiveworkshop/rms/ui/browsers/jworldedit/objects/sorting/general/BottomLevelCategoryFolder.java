package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.TreeNodeLinker;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Comparator;

public final class BottomLevelCategoryFolder extends SortingFolderTreeNode {
	private final Comparator<MutableGameObject> objectComparator;

	public BottomLevelCategoryFolder(Object userObject, Comparator<MutableGameObject> objectComparator) {
		super(userObject);
		this.objectComparator = objectComparator;
	}

	@Override
	public DefaultMutableTreeNode add(MutableGameObject unitToAdd, TreeNodeLinker treeNodeLinker) {
		DefaultMutableTreeNode unitNode = new DefaultMutableTreeNode(unitToAdd);
		int insertIndex = getInsertIndex(unitToAdd);
		treeNodeLinker.insertNodeInto(unitNode, this, insertIndex);
		return unitNode;
	}

	private int getInsertIndex(MutableGameObject unitToAdd) {
		int insertIndex = 0;
		for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
			MutableGameObject unitInTree = getGameObjectAt(childIndex);
			int comparison = objectComparator.compare(unitToAdd, unitInTree);
			if (0 <= comparison) {
				insertIndex = childIndex + 1;
			}
		}
		return insertIndex;
	}

	private MutableGameObject getGameObjectAt(int childIndex) {
		return ((MutableGameObject) ((DefaultMutableTreeNode) getChildAt(childIndex)).getUserObject());
	}

	public SortingFolderTreeNode getNextNode(MutableGameObject object, TreeNodeLinker defaultTreeModel) {
		return null;
	}

	//	@Override
	public int getSortIndex(SortingFolderTreeNode childNode) {
		return 0;
	}

	@Override
	public int getSortIndex(TreeNode childNode) {
		return 0;
	}
}
