package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.util.War3ID;

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
		if (root instanceof DefaultMutableTreeNode) {
			unitIdToNode.clear();
			init((DefaultMutableTreeNode) root);
		}
	}

	private void init(final DefaultMutableTreeNode root) {
		final Enumeration<TreeNode> depthFirstEnumeration = root.depthFirstEnumeration();
		while (depthFirstEnumeration.hasMoreElements()) {
			final Object nextElement = depthFirstEnumeration.nextElement();
			if (nextElement instanceof DefaultMutableTreeNode) {
				final DefaultMutableTreeNode objectNode = (DefaultMutableTreeNode) nextElement;
				final Object userObject = objectNode.getUserObject();
				if (userObject instanceof MutableGameObject) {
					final MutableGameObject gameObject = (MutableGameObject) userObject;
					final War3ID alias = gameObject.getAlias();
					unitIdToNode.put(alias, objectNode);
				}
			}
		}
	}

	@Override
	public void insertNodeInto(final MutableTreeNode newChild, final MutableTreeNode parent, final int index) {
		super.insertNodeInto(newChild, parent, index);
		if (newChild instanceof DefaultMutableTreeNode) {
			final DefaultMutableTreeNode objectNode = (DefaultMutableTreeNode) newChild;
			final Object userObject = objectNode.getUserObject();
			if (userObject instanceof MutableGameObject) {
				final MutableGameObject gameObject = (MutableGameObject) userObject;
				final War3ID alias = gameObject.getAlias();
				unitIdToNode.put(alias, objectNode);
			}
		}
	}

	@Override
	public void removeNodeFromParent(final MutableTreeNode node) {
		super.removeNodeFromParent(node);
		if (node instanceof DefaultMutableTreeNode) {
			final DefaultMutableTreeNode objectNode = (DefaultMutableTreeNode) node;
			final Object userObject = objectNode.getUserObject();
			if (userObject instanceof MutableGameObject) {
				final MutableGameObject gameObject = (MutableGameObject) userObject;
				final War3ID alias = gameObject.getAlias();
				unitIdToNode.remove(alias);
			}
		}
	}

	public MutableTreeNode getNodeById(final War3ID id) {
		return unitIdToNode.get(id);
	}
}
