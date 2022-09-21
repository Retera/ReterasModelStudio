package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;

import javax.swing.tree.DefaultMutableTreeNode;

public abstract class AbstractSortingFolderTreeNode extends SortingFolderTreeNode {
	private boolean hasExpandedFirstTime = false;
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
//	private static final long serialVersionUID = 1L;

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
	public DefaultMutableTreeNode add(MutableGameObject mutableGameObject, TreeNodeLinker treeNodeLinker) {
		DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(mutableGameObject);
		treeNodeLinker.insertNodeInto(newChild, this, getChildCount());
		return newChild;
	}

	public SortingFolderTreeNode getNextNode(MutableGameObject object, TreeNodeLinker treeModel) {
		SortingFolderTreeNode sortingFolderTreeNode = getNextNode(object);
		if (sortingFolderTreeNode != null && !isNodeChild(sortingFolderTreeNode)) {
			int sortedInsertionIndex = getInsertIndex(sortingFolderTreeNode);
			treeModel.insertNodeInto(sortingFolderTreeNode, this, sortedInsertionIndex);
		}
		return sortingFolderTreeNode;
	}

	private int getInsertIndex(SortingFolderTreeNode sortingFolderTreeNode) {
		int childCount = getChildCount();
		if(0 < childCount){
			int sortIndex = getSortIndex(sortingFolderTreeNode);

			int sortedInsertionIndex = childCount;
			for (int i = 0; i < childCount; i++) {
				if(sortIndex < getSortIndex(getChildAt(i))){
					sortedInsertionIndex = i;
					break;
				}
			}
			return sortedInsertionIndex;
		}
		return 0;
	}

	protected abstract SortingFolderTreeNode getNextNode(MutableGameObject object);

}
