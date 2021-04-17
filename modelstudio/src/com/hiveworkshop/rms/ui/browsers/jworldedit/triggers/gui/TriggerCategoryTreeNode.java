package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui;

import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.Trigger;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.TriggerCategory;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.HashMap;
import java.util.Map;

public class TriggerCategoryTreeNode extends DefaultMutableTreeNode implements TriggerElementTreeNode {

	private final TriggerCategory category;
	private final Map<Trigger, TriggerTreeNode> triggerToNode;

	public TriggerCategoryTreeNode(final TriggerCategory category) {
		super(category.getName());
		this.category = category;
		triggerToNode = new HashMap<>();
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

	@Override
	public TriggerElementTreeNode copy() {
		return new TriggerCategoryTreeNode(category);
	}

	@Override
	public String toString() {
		return "TCTN: " + super.toString();
	}
}
