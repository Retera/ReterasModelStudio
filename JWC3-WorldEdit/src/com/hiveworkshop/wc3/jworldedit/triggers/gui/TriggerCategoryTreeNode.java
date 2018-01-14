package com.hiveworkshop.wc3.jworldedit.triggers.gui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.hiveworkshop.wc3.jworldedit.triggers.impl.Trigger;
import com.hiveworkshop.wc3.jworldedit.triggers.impl.TriggerCategory;

public class TriggerCategoryTreeNode extends DefaultMutableTreeNode {

	private final TriggerCategory category;
	private final Map<Trigger, TriggerTreeNode> triggerToNode;

	public TriggerCategoryTreeNode(final TriggerCategory category) {
		super(category.getName());
		this.category = category;
		this.triggerToNode = new HashMap<>();
		for (final Trigger trigger : category.getTriggers()) {
			add(createChild(trigger));
		}
	}

	public TriggerCategory getCategory() {
		return category;
	}

	public TriggerTreeNode getNode(final Trigger trigger) {
		return triggerToNode.get(trigger);
	}

	public void add(final Trigger trigger, final DefaultTreeModel treeModel) {
		final TriggerTreeNode newChild = createChild(trigger);
		treeModel.insertNodeInto(newChild, this, 0);
	}

	private TriggerTreeNode createChild(final Trigger trigger) {
		final TriggerTreeNode newChild = new TriggerTreeNode(trigger);
		triggerToNode.put(trigger, newChild);
		return newChild;
	}

}
