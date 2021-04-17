package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;

public abstract class SortingFolderTreeNode extends DefaultMutableTreeNode implements GameObjectSortingFolder {
	/**
	 * default generated id to stop warnings, not going to serialize these folders
	 */
	private static final long serialVersionUID = 1L;

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

	public abstract int getSortIndex(DefaultMutableTreeNode childNode);

	/**
	 * Returns the total number of leaves that are descendants of this node. If this node is a leaf, returns
	 * <code>1</code>. This method is O(n) where n is the number of descendants of this node.
	 *
	 * @see #isNodeAncestor
	 * @return the number of leaves beneath this node
	 */
	@Override
	public int getLeafCount() {
		int count = 0;

		TreeNode node;
		final Enumeration<TreeNode> enum_ = breadthFirstEnumeration(); // order matters not

		while (enum_.hasMoreElements()) {
			node = enum_.nextElement();
			if (node.isLeaf() && !(node instanceof SortingFolderTreeNode)) { // override: sorting folders don't count as
				// leaves
				count++;
			}
		}

		return count;
	}

	public boolean hasEditedChildren() {
		TreeNode node;
		final Enumeration<TreeNode> enum_ = breadthFirstEnumeration(); // order matters not

		while (enum_.hasMoreElements()) {
			node = enum_.nextElement();
			if ((node instanceof DefaultMutableTreeNode)) { // override: sorting folders don't count as
				// leaves
				final DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) node;
				if (mutableTreeNode.getUserObject() instanceof MutableGameObject) {
					final MutableGameObject gameObject = (MutableGameObject) mutableTreeNode.getUserObject();
					if (gameObject.hasEditorData()) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
