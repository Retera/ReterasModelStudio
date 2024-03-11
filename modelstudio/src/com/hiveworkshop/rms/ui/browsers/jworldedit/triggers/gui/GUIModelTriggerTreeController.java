package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui;

import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.Trigger;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.TriggerCategory;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.Objects;

public class GUIModelTriggerTreeController implements TriggerTreeController {
	private final TriggerTree triggerTree;
	private final TriggerTreeController delegate;
	private final TriggerEnvironmentRootNode root;
	private final DefaultTreeModel treeModel;

	public GUIModelTriggerTreeController(TriggerTree triggerTree, final TriggerTreeController delegate,
	                                     final TriggerEnvironmentRootNode root, final DefaultTreeModel treeModel) {
		this.triggerTree = triggerTree;
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
		final TriggerTreeNode triggerNode = root.getNode(trigger.getCategory()) == null ? null : root.getNode(trigger.getCategory()).getNode(trigger);
		delegate.moveTrigger(trigger, triggerCategory, index);
		if (triggerNode != null) {
			treeModel.removeNodeFromParent(triggerNode);
		}
		final TriggerCategoryTreeNode categoryNode = root.getNode(triggerCategory);
		treeModel.insertNodeInto(Objects.requireNonNullElseGet(triggerNode, () -> new TriggerTreeNode(trigger)), categoryNode, index);
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

	@Override
	public void deleteTrigger(final Trigger trigger) {
		final TriggerCategoryTreeNode categoryNode = root.getNode(trigger.getCategory());
		final TriggerTreeNode nodeToDelete = categoryNode.getNode(trigger);
		final TreePath selectionPath = triggerTree.getSelectionPath();
		TreePath newSelectionPath = null;
		if (triggerTree.getSelectionCount() == 1) {
			if (selectionPath.getLastPathComponent() == nodeToDelete) {
				final int nextChildIndex = categoryNode.getIndex(nodeToDelete) + 1;
				final int triggersInCategoryCount = categoryNode.getChildCount();
				if (triggersInCategoryCount == 1) {
					newSelectionPath = selectionPath.getParentPath();
				} else {
					newSelectionPath = selectionPath.getParentPath().pathByAddingChild(categoryNode.getChildAt(Math.min(nextChildIndex, triggersInCategoryCount - 2)));
				}
			}
		}
		treeModel.removeNodeFromParent(nodeToDelete);
		delegate.deleteTrigger(trigger);
		if (newSelectionPath != null) {
			triggerTree.setSelectionPath(newSelectionPath);
		}
	}

	@Override
	public void deleteCategory(final TriggerCategory category) {
		final TriggerCategoryTreeNode categoryNode = root.getNode(category);
		final TreePath selectionPath = triggerTree.getSelectionPath();
		TreePath newSelectionPath = null;
		if (triggerTree.getSelectionCount() == 1) {
			if (selectionPath.getLastPathComponent() == categoryNode) {
				final int nextChildIndex = root.getIndex(categoryNode) + 1;
				final int categoryCount = root.getChildCount();
				if (categoryCount == 1) {
					newSelectionPath = selectionPath.getParentPath();
				} else {
					newSelectionPath = selectionPath.getParentPath().pathByAddingChild(root.getChildAt(Math.min(nextChildIndex, categoryCount - 2)));
				}
			}
		}
		treeModel.removeNodeFromParent(categoryNode);
		delegate.deleteCategory(category);
		if (newSelectionPath != null) {
			triggerTree.setSelectionPath(newSelectionPath);
		}
	}

	@Override
	public void moveCategory(final TriggerCategory triggerCategory, final int index) {
		final TriggerCategoryTreeNode categoryNode = root.getNode(triggerCategory);
		delegate.moveCategory(triggerCategory, index);
		if (categoryNode != null) {
			treeModel.removeNodeFromParent(categoryNode);
		}
		treeModel.insertNodeInto(categoryNode, root, index);
	}
}
