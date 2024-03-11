package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;

public abstract class SortingFolderTreeNode extends DefaultMutableTreeNode {

	public SortingFolderTreeNode() {
		super();
	}

	public SortingFolderTreeNode(final Object userObject, final boolean allowsChildren) {
		super(userObject, allowsChildren);
	}

	public SortingFolderTreeNode(final Object userObject) {
		super(userObject);
	}

	public abstract DefaultMutableTreeNode add(final MutableGameObject mutableGameObject, TreeNodeLinker treeModel);

	public abstract int getSortIndex(SortingFolderTreeNode childNode);
	public int getSortIndex() {
		if (parent instanceof SortingFolderTreeNode sortingNode) {
			return sortingNode.getSortIndex(this);
		}
		return 0;
	}

	public abstract int getSortIndex(TreeNode childNode);

	/**
	 * Returns the total number of leaves that are descendants of this node. If this node is a leaf, returns
	 * <code>1</code>. This method is O(n) where n is the number of descendants of this node.
	 *
	 * @return the number of leaves beneath this node
	 * @see #isNodeAncestor
	 */
	@Override
	public int getLeafCount() {
		int count = 0;

		final Enumeration<TreeNode> enum_ = breadthFirstEnumeration(); // order matters not

		while (enum_.hasMoreElements()) {
			TreeNode node = enum_.nextElement();
			if (node.isLeaf() && !(node instanceof SortingFolderTreeNode)) {
				// override: sorting folders don't count as leaves
				count++;
			}
		}

		return count;
	}

	public boolean hasEditedChildren() {
		final Enumeration<TreeNode> enum_ = breadthFirstEnumeration(); // order matters not

		while (enum_.hasMoreElements()) {
			TreeNode node = enum_.nextElement();
			if (node instanceof final DefaultMutableTreeNode treeNode  // override: sorting folders don't count as leaves
					&& treeNode.getUserObject() instanceof final MutableGameObject gameObject
					&& gameObject.hasEditorData()) {
				return true;
			}
		}

		return false;
	}

	public abstract SortingFolderTreeNode getNextNode(MutableGameObject object, TreeNodeLinker treeModel);
}
