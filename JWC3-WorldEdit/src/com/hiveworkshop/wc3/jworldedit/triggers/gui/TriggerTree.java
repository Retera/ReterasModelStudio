package com.hiveworkshop.wc3.jworldedit.triggers.gui;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.hiveworkshop.wc3.jworldedit.WorldEditArt;
import com.hiveworkshop.wc3.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.wc3.jworldedit.triggers.TriggerTreeCellRenderer;
import com.hiveworkshop.wc3.jworldedit.triggers.impl.Trigger;
import com.hiveworkshop.wc3.jworldedit.triggers.impl.TriggerCategory;
import com.hiveworkshop.wc3.jworldedit.triggers.impl.TriggerEnvironment;
import com.hiveworkshop.wc3.units.DataTable;

public class TriggerTree extends JTree {
	private final TriggerEnvironment triggerEnvironment;
	private final TriggerEnvironmentRootNode root;
	private final GUIModelTriggerTreeController controller;

	public TriggerTree(final TriggerEnvironment triggerEnvironment) {
		super(new TriggerEnvironmentRootNode(triggerEnvironment));
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		root = (TriggerEnvironmentRootNode) ((DefaultTreeModel) getModel()).getRoot();
		this.triggerEnvironment = triggerEnvironment;
		setCellRenderer(new TriggerTreeCellRenderer(new UnitEditorSettings(),
				new WorldEditArt(DataTable.getWorldEditorData())));
		controller = new GUIModelTriggerTreeController(triggerEnvironment, root, ((DefaultTreeModel) getModel()));
		setEditable(true);
		setDragEnabled(true);
	}

	public GUIModelTriggerTreeController getController() {
		return controller;
	}

	public void select(final TriggerCategory category) {
		final TriggerCategoryTreeNode node = root.getNode(category);
		setSelectionPath(new TreePath(new Object[] { root, node }));
	}

	public void select(final Trigger trigger) {
		final TriggerCategoryTreeNode categoryNode = root.getNode(trigger.getCategory());
		final TriggerTreeNode triggerNode = categoryNode.getNode(trigger);
		setSelectionPath(new TreePath(new Object[] { root, categoryNode, triggerNode }));
	}

	public static class GUIModelTriggerTreeController implements TriggerTreeController {
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

	public Trigger createTrigger() {
		return createTrigger(TypedTriggerInstantiator.TRIGGER);
	}

	public Trigger createTriggerComment() {
		return createTrigger(TypedTriggerInstantiator.COMMENT);
	}

	private static enum TypedTriggerInstantiator {
		TRIGGER() {
			@Override
			public Trigger create(final TriggerTreeController controller, final TriggerCategory category) {
				return controller.createTrigger(category);
			}
		},
		COMMENT() {
			@Override
			public Trigger create(final TriggerTreeController controller, final TriggerCategory category) {
				return controller.createTriggerComment(category);
			}
		};
		public abstract Trigger create(TriggerTreeController controller, TriggerCategory category);
	};

	private Trigger createTrigger(final TypedTriggerInstantiator instantiator) {
		final TreePath selectionPath = getSelectionPath();
		if (!canCreateTrigger(selectionPath)) {
			throw new IllegalStateException("Cannot create trigger at selection");
		}
		final Object lastPathComponent = selectionPath.getLastPathComponent();
		if (lastPathComponent instanceof TriggerCategoryTreeNode) {
			// category
			final TriggerCategoryTreeNode node = (TriggerCategoryTreeNode) lastPathComponent;
			return instantiator.create(controller, node.getCategory());
		} else if (lastPathComponent instanceof TriggerTreeNode) {
			final TriggerTreeNode node = (TriggerTreeNode) lastPathComponent;
			final int newTriggerIndex = node.getParent().getIndex(node) + 1;
			final Trigger trigger = instantiator.create(controller, node.getTrigger().getCategory());
			controller.moveTrigger(trigger, trigger.getCategory(), newTriggerIndex);
			return trigger;
		} else {
			throw new IllegalStateException("Cannot create trigger with selection");
		}
	}

	public boolean canCreateTrigger(final TreePath selectionPath) {
		return selectionPath.getPathCount() >= 2;
	}
}