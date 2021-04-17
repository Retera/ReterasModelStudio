package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general;

import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.SortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.TreeNodeLinker;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;

public final class BottomLevelCategoryFolder extends SortingFolderTreeNode {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;

	private final Comparator<MutableGameObject> objectComparator;

	public BottomLevelCategoryFolder(final Object userObject, final Comparator<MutableGameObject> objectComparator) {
		super(userObject);
		this.objectComparator = objectComparator;
	}

	@Override
	public DefaultMutableTreeNode add(final MutableGameObject unitToAdd, final TreeNodeLinker treeNodeLinker) {
		final DefaultMutableTreeNode unitNode = new DefaultMutableTreeNode(unitToAdd);
		int insertIndex = 0;
		for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
			final TreeNode child = getChildAt(childIndex);
			final MutableGameObject unitInTree = ((MutableGameObject) ((DefaultMutableTreeNode) child).getUserObject());
			final int comparison = objectComparator.compare(unitToAdd, unitInTree);
			if (comparison >= 0) {
				insertIndex = childIndex + 1;
			}
		}
		treeNodeLinker.insertNodeInto(unitNode, this, insertIndex);
		return unitNode;
	}

	@Override
	public SortingFolderTreeNode getNextNode(final MutableGameObject object, final TreeNodeLinker defaultTreeModel) {
		return null;
	}

	@Override
	public int getSortIndex(final DefaultMutableTreeNode childNode) {
		return 0;
	}
}
