package com.hiveworkshop.rms.ui.browsers.mpq;

import javax.swing.tree.TreeNode;
import java.util.*;
import java.util.stream.Collectors;

public class MPQTreeNode implements TreeNode {
	private final MPQTreeNode parent;
	private final String path;
	private final String extension;
	private final String subPathName;
	private final Map<String, MPQTreeNode> children;
	private final Map<String, MPQTreeNode> hiddenChildren;
	private final List<String> childrenKeys;
	private Boolean isVisible;

	public MPQTreeNode(final MPQTreeNode parent, final String path, final String subPathName, final String extension) {
		this.parent = parent;
		this.path = path;
		this.subPathName = subPathName;
		children = new HashMap<>();
		hiddenChildren = new HashMap<>();
		childrenKeys = new ArrayList<>();
		this.extension = extension;
		isVisible = true;
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

	public int getTotalChildCount() {
		return children.size() + hiddenChildren.size();
	}

	public MPQTreeNode getChild(final String subPathName) {
		return children.get(subPathName);
	}

	public List<MPQTreeNode> getChildren() {
		return new ArrayList<MPQTreeNode>(children.values());
	}

	public List<MPQTreeNode> getHiddenChildren() {
		return new ArrayList<MPQTreeNode>(hiddenChildren.values());
	}

	/**
	 * Adds a child under the tree. Currently doesn't fix duplicates, which will
	 * break the state of the system.
	 */
	public void addChild(final String subPathName, final MPQTreeNode child) {
		final MPQTreeNode prev = children.put(subPathName, child);
		if (prev == null) {
			childrenKeys.add(child.getSubPathName());
		}
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean visible) {
		isVisible = visible;
	}

	public String getExtension() {
		return extension;
	}

	public boolean hasVisibleChildren() {
		return children.values().stream().anyMatch(c -> c.isVisible);
	}

	public List<MPQTreeNode> getVisibleChildren() {
		return children.values().stream().filter(c -> c.isVisible).collect(Collectors.toList());
	}

	public void updateChildrenVisibility() {
		setHiddenChildren();
		setVisibleChildren();
	}

	public void setHiddenChildren() {
		List<MPQTreeNode> childrenToHide = children.values().stream().filter(c -> !c.isVisible).collect(Collectors.toList());

		for (MPQTreeNode child : childrenToHide) {
			hiddenChildren.put(child.getSubPathName(), child);
			children.remove(child.getSubPathName());
			childrenKeys.remove(child.getSubPathName());
		}
	}

	public void setVisibleChildren() {
		List<MPQTreeNode> childrenToShow = hiddenChildren.values().stream().filter(c -> c.isVisible).collect(Collectors.toList());
		for (MPQTreeNode child : childrenToShow) {
			children.put(child.getSubPathName(), child);
			hiddenChildren.remove(child.getSubPathName());
			childrenKeys.add(child.getSubPathName());
		}
	}

	@Override
	public int getIndex(final TreeNode node) {
		if (!(node instanceof MPQTreeNode)) {
			return -1;
		}

		return childrenKeys.indexOf(((MPQTreeNode) node).getPath());
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
