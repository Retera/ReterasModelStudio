package com.hiveworkshop.wc3.jworldedit.objects;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public class UnitEditorTreeModel extends DefaultTreeModel {
	private final Map<War3ID, MutableTreeNode> unitIdToNode = new HashMap<>();

	public UnitEditorTreeModel(final DefaultMutableTreeNode root, final boolean asksAllowsChildren) {
		super(root, asksAllowsChildren);
	}

	public UnitEditorTreeModel(final DefaultMutableTreeNode root) {
		super(root);
	}

	private void init(final DefaultMutableTreeNode root) {
		final Enumeration depthFirstEnumeration = root.depthFirstEnumeration();
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
