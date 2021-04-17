package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui;

import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.TriggerCategory;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.TriggerEnvironment;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.HashMap;
import java.util.Map;

public final class TriggerEnvironmentRootNode extends DefaultMutableTreeNode implements TriggerElementTreeNode {
	private final Map<TriggerCategory, TriggerCategoryTreeNode> categoryToNode = new HashMap<>();
	private final TriggerEnvironment triggerEnvironment;

	public TriggerEnvironmentRootNode(final TriggerEnvironment triggerEnvironment) {
		super(triggerEnvironment.getName());
		this.triggerEnvironment = triggerEnvironment;
		for (final TriggerCategory category : triggerEnvironment.getCategories()) {
			add(createChild(category));
		}
	}

	public TriggerEnvironment getTriggerEnvironment() {
		return triggerEnvironment;
	}

	public TriggerCategoryTreeNode getNode(final TriggerCategory category) {
		return categoryToNode.get(category);
	}

	public void add(final TriggerCategory category, final DefaultTreeModel treeModel) {
		final TriggerCategoryTreeNode newChild = createChild(category);
		treeModel.insertNodeInto(newChild, this, getChildCount());
	}

	private TriggerCategoryTreeNode createChild(final TriggerCategory category) {
		final TriggerCategoryTreeNode newChild = new TriggerCategoryTreeNode(category);
		categoryToNode.put(category, newChild);
		return newChild;
	}

	@Override
	public TriggerElementTreeNode copy() {
		throw new UnsupportedOperationException();
	}
}