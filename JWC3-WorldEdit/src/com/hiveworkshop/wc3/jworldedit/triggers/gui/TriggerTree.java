package com.hiveworkshop.wc3.jworldedit.triggers.gui;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import com.hiveworkshop.wc3.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.wc3.jworldedit.triggers.TriggerTreeCellRenderer;
import com.hiveworkshop.wc3.jworldedit.triggers.impl.Trigger;
import com.hiveworkshop.wc3.jworldedit.triggers.impl.TriggerCategory;
import com.hiveworkshop.wc3.jworldedit.triggers.impl.TriggerEnvironment;

public class TriggerTree extends JTree {
	private final TriggerEnvironment triggerEnvironment;
	private final TriggerEnvironmentRootNode root;
	private final GUIModelTriggerTreeController controller;

	public TriggerTree(final TriggerEnvironment triggerEnvironment) {
		super(new TriggerEnvironmentRootNode(triggerEnvironment));
		root = (TriggerEnvironmentRootNode) ((DefaultTreeModel) getModel()).getRoot();
		this.triggerEnvironment = triggerEnvironment;
		setCellRenderer(new TriggerTreeCellRenderer(new UnitEditorSettings()));
		controller = new GUIModelTriggerTreeController(triggerEnvironment, root, ((DefaultTreeModel) getModel()));
	}

	public GUIModelTriggerTreeController getController() {
		return controller;
	}

	private static class GUIModelTriggerTreeController implements TriggerTreeController {
		private final TriggerTreeController delegate;
		private final TriggerEnvironmentRootNode root;
		private final DefaultTreeModel treeModel;

		public GUIModelTriggerTreeController(final TriggerTreeController delegate,
				final TriggerEnvironmentRootNode root, final DefaultTreeModel treeModel) {
			this.delegate = delegate;
			this.root = root;
			this.treeModel = treeModel;
		}

		@Override
		public Trigger createTrigger(final TriggerCategory triggerCategory) {
			final Trigger trigger = delegate.createTrigger(triggerCategory);
			root.getNode(triggerCategory).add(trigger, treeModel);
			return trigger;
		}

		@Override
		public Trigger createTriggerComment(final TriggerCategory triggerCategory) {
			final Trigger triggerComment = delegate.createTriggerComment(triggerCategory);
			root.getNode(triggerCategory).add(triggerComment, treeModel);
			return triggerComment;
		}

		@Override
		public TriggerCategory createCategory() {
			final TriggerCategory category = delegate.createCategory();
			root.add(category, treeModel);
			return category;
		}

		@Override
		public void renameTrigger(final Trigger trigger, final String name) {
			delegate.renameTrigger(trigger, name);
			final TriggerTreeNode nodeForTrigger = root.getNode(trigger.getCategory()).getNode(trigger);
			nodeForTrigger.setUserObject(name);
			treeModel.nodeChanged(nodeForTrigger);
		}

		@Override
		public void moveTrigger(final Trigger trigger, final TriggerCategory triggerCategory, final int index) {
			final TriggerTreeNode triggerNode = root.getNode(trigger.getCategory()).getNode(trigger);
			delegate.moveTrigger(trigger, triggerCategory, index);
			treeModel.removeNodeFromParent(triggerNode);
			final TriggerCategoryTreeNode categoryNode = root.getNode(triggerCategory);
			treeModel.insertNodeInto(triggerNode, categoryNode, index);
		}

		@Override
		public void toggleCategoryIsComment(final TriggerCategory triggerCategory) {
			delegate.toggleCategoryIsComment(triggerCategory);
			treeModel.nodeChanged(root.getNode(triggerCategory));
		}

		@Override
		public void renameCategory(final TriggerCategory trigger, final String name) {
			delegate.renameCategory(trigger, name);
			final TriggerCategoryTreeNode node = root.getNode(trigger);
			node.setUserObject(name);
			treeModel.nodeChanged(node);
		}

	}
}