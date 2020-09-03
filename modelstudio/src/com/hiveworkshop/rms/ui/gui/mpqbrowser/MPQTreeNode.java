package com.hiveworkshop.rms.ui.gui.mpqbrowser;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreeNode;

public class MPQTreeNode implements TreeNode {
	private final MPQTreeNode parent;
	private final String path;
	private final String subPathName;
	private final Map<String, MPQTreeNode> children;
	private final List<String> childrenKeys;

	public MPQTreeNode(final MPQTreeNode parent, final String path, final String subPathName) {
		this.parent = parent;
		this.path = path;
		this.subPathName = subPathName;
		this.children = new HashMap<>();
		this.childrenKeys = new ArrayList<>();
	}

	@Override
	public Enumeration<MPQTreeNode> children() {
		return new Enumeration<>() {
			private final Iterator<MPQTreeNode> iterator = children.values().iterator();

			@Override
			public boolean hasMoreElements() {
				return iterator.hasNext();
			}

			@Override
			public MPQTreeNode nextElement() {
				return iterator.next();
			}
		};
	}

	public String getPath() {
		return path;
	}

	public String getSubPathName() {
		return subPathName;
	}

	@Override
	public boolean getAllowsChildren() {
		return !path.contains("."); // nonfile?
	}

	@Override
	public TreeNode getChildAt(final int index) {
		return children.get(childrenKeys.get(index));
	}

	@Override
	public int getChildCount() {
		return children.size();
	}

	public MPQTreeNode getChild(final String subPathName) {
		return children.get(subPathName);
	}

	/**
	 * Adds a child under the tree. Currently doesn't fix duplicates, which will
	 * break the state of the system.
	 *
	 * @param subPathName
	 * @param child
	 */
	public void addChild(final String subPathName, final MPQTreeNode child) {
		final MPQTreeNode prev = children.put(subPathName, child);
		if (prev == null) {
			childrenKeys.add(child.getSubPathName());
		}
	}

	@Override
	public int getIndex(final TreeNode node) {
		if (!(node instanceof MPQTreeNode)) {
			return -1;
		}

		return childrenKeys.indexOf(((MPQTreeNode)node).getPath());
	}

	@Override
	public TreeNode getParent() {
		return parent;
	}

	@Override
	public boolean isLeaf() {
		return children.size() == 0;
	}

	@Override
	public String toString() {
		return subPathName;
	}

	public void sort() {
		childrenKeys.sort((o1, o2) -> {
			final MPQTreeNode child1 = children.get(o1);
			final MPQTreeNode child2 = children.get(o2);
			if (child1.isLeaf() && !child2.isLeaf()) {
				return 1;
			} else if (!child1.isLeaf() && child2.isLeaf()) {
				return -1;
			}
			return o1.compareTo(o2);
		});
		for (final MPQTreeNode child : children.values()) {
			if (!child.isLeaf()) {
				child.sort();
			}
		}
	}
}
