package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.TreeNodeLinker;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Comparator;

public final class BottomLevelCategoryFolder extends SortingFolderTreeNode {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;

	private final Comparator<MutableGameObject> objectComparator;

	public BottomLevelCategoryFolder(Object userObject, Comparator<MutableGameObject> objectComparator) {
		super(userObject);
		this.objectComparator = objectComparator;
	}

	@Override
	public DefaultMutableTreeNode add(MutableGameObject unitToAdd, TreeNodeLinker treeNodeLinker) {
		DefaultMutableTreeNode unitNode = new DefaultMutableTreeNode(unitToAdd);
		int insertIndex = 0;
		for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
			TreeNode child = getChildAt(childIndex);
			MutableGameObject unitInTree = ((MutableGameObject) ((DefaultMutableTreeNode) child).getUserObject());
			int comparison = objectComparator.compare(unitToAdd, unitInTree);
			if (comparison >= 0) {
				insertIndex = childIndex + 1;
			}
		}
		treeNodeLinker.insertNodeInto(unitNode, this, insertIndex);
		return unitNode;
	}

	@Override
	public SortingFolderTreeNode getNextNode(MutableGameObject object, TreeNodeLinker defaultTreeModel) {
		return null;
	}

	@Override
	public int getSortIndex(final DefaultMutableTreeNode childNode) {
		return 0;
	}
}
