package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class UnitEditorTreeModel extends DefaultTreeModel {
	private final Map<War3ID, MutableTreeNode> unitIdToNode = new HashMap<>();

	public UnitEditorTreeModel(final DefaultMutableTreeNode root, final boolean asksAllowsChildren) {
		super(root, asksAllowsChildren);
		init(root);
	}

	public UnitEditorTreeModel(final DefaultMutableTreeNode root) {
		super(root);
		init(root);
	}

	@Override
	public void setRoot(final TreeNode root) {
		super.setRoot(root);
		if (root instanceof DefaultMutableTreeNode treeRoot) {
			unitIdToNode.clear();
			init(treeRoot);
		}
	}

	private void init(final DefaultMutableTreeNode root) {
		final Enumeration<TreeNode> depthFirstEnumeration = root.depthFirstEnumeration();
		while (depthFirstEnumeration.hasMoreElements()) {
			final Object nextElement = depthFirstEnumeration.nextElement();
			if (nextElement instanceof final DefaultMutableTreeNode objectNode
					&& objectNode.getUserObject() instanceof final MutableGameObject gameObject) {
				final War3ID alias = gameObject.getAlias();
				unitIdToNode.put(alias, objectNode);
			}
		}
	}

	@Override
	public void insertNodeInto(final MutableTreeNode newChild, final MutableTreeNode parent, final int index) {
		super.insertNodeInto(newChild, parent, index);
		if (newChild instanceof final DefaultMutableTreeNode objectNode
				&& objectNode.getUserObject() instanceof final MutableGameObject gameObject) {
			final War3ID alias = gameObject.getAlias();
			unitIdToNode.put(alias, objectNode);
		}
	}

	@Override
	public void removeNodeFromParent(final MutableTreeNode node) {
		super.removeNodeFromParent(node);
		if (node instanceof final DefaultMutableTreeNode objectNode
				&& objectNode.getUserObject() instanceof final MutableGameObject gameObject) {
			final War3ID alias = gameObject.getAlias();
			unitIdToNode.remove(alias);
		}
	}

	public MutableTreeNode getNodeById(final War3ID id) {
		return unitIdToNode.get(id);
	}
}
