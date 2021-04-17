package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting;

import javax.swing.tree.DefaultMutableTreeNode;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;

public abstract class AbstractSortingFolderTreeNode extends SortingFolderTreeNode {
	private boolean hasExpandedFirstTime = false;
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;

	public AbstractSortingFolderTreeNode() {
		super();
	}

	public AbstractSortingFolderTreeNode(final Object userObject, final boolean allowsChildren) {
		super(userObject, allowsChildren);
	}

	public AbstractSortingFolderTreeNode(final Object userObject) {
		super(userObject);
	}

	public boolean isHasExpandedFirstTime() {
		return hasExpandedFirstTime;
	}

	public void setHasExpandedFirstTime(final boolean hasExpandedFirstTime) {
		this.hasExpandedFirstTime = hasExpandedFirstTime;
	}

	@Override
	public DefaultMutableTreeNode add(final MutableGameObject mutableGameObject, final TreeNodeLinker treeModel) {
		final DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(mutableGameObject);
		treeModel.insertNodeInto(newChild, this, getChildCount());
		return newChild;
	}

	@Override
	public SortingFolderTreeNode getNextNode(final MutableGameObject object, final TreeNodeLinker treeModel) {
		final SortingFolderTreeNode sortingFolderTreeNode = getNextNode(object);
		if (sortingFolderTreeNode == null) {
			return sortingFolderTreeNode;
		}
		int sortedInsertionIndex = 0;
		for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
			if (getSortIndex(sortingFolderTreeNode) >= getSortIndex((DefaultMutableTreeNode) getChildAt(childIndex))) {
				sortedInsertionIndex = childIndex + 1;
			}
		}
		if (!isNodeChild(sortingFolderTreeNode)) {
			treeModel.insertNodeInto(sortingFolderTreeNode, this, sortedInsertionIndex);
		}
		return sortingFolderTreeNode;
	}

	protected abstract SortingFolderTreeNode getNextNode(MutableGameObject object);

}
