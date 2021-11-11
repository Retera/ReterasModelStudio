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
	public DefaultMutableTreeNode add(MutableGameObject mutableGameObject, TreeNodeLinker treeModel) {
		DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(mutableGameObject);
		treeModel.insertNodeInto(newChild, this, getChildCount());
		return newChild;
	}

	public SortingFolderTreeNode getNextNode(MutableGameObject object, TreeNodeLinker treeModel) {
		SortingFolderTreeNode sortingFolderTreeNode = getNextNode(object);
		if (sortingFolderTreeNode != null) {
			int sortedInsertionIndex = 0;
			for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
				if (getSortIndex(sortingFolderTreeNode) >= getSortIndex(getChildAt(childIndex))) {
					sortedInsertionIndex = childIndex + 1;
				}
			}
			if (!isNodeChild(sortingFolderTreeNode)) {
				treeModel.insertNodeInto(sortingFolderTreeNode, this, sortedInsertionIndex);
			}
		}
		return sortingFolderTreeNode;
	}

//	@Override
//	public SortingFolderTreeNode getChildAt(int index){
//		TreeNode childAt = super.getChildAt(index);
//		if(childAt instanceof  SortingFolderTreeNode){
//			return (SortingFolderTreeNode) childAt;
//		} else {
//			if(childAt != null && childAt.toString().equals("com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject@15a902e7")){
//				System.out.println("index: " + index);
//				System.out.println("childAt: " + childAt);
//				System.out.println("childClass: " + childAt.getClass());
//				new Exception().printStackTrace();
//			}
//		}
//		return null;
//	}

	protected abstract SortingFolderTreeNode getNextNode(MutableGameObject object);

}
