package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui;

import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WorldEditArt;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WorldEditorSettings;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.TriggerTreeCellEditor;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.TriggerTreeCellRenderer;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.Trigger;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.TriggerCategory;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.TriggerEnvironment;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class TriggerTree extends JTree {
	private final TriggerEnvironment triggerEnvironment;
	private final TriggerEnvironmentRootNode root;
	private final GUIModelTriggerTreeController controller;

	public TriggerTree(final TriggerEnvironment triggerEnvironment) {
		super(new TriggerEnvironmentRootNode(triggerEnvironment));
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		root = (TriggerEnvironmentRootNode) getModel().getRoot();
		this.triggerEnvironment = triggerEnvironment;
		final WorldEditArt worldEditArt = new WorldEditArt(DataTable.getWorldEditorData());
		final WorldEditorSettings settings = new WorldEditorSettings();
		final TriggerTreeCellRenderer triggerTreeCellRenderer = new TriggerTreeCellRenderer(settings, worldEditArt);
		setCellRenderer(triggerTreeCellRenderer);
		final TriggerTreeCellEditor treeCellEditor = new TriggerTreeCellEditor(this, triggerTreeCellRenderer, settings,
				worldEditArt, triggerEnvironment);
		setCellEditor(treeCellEditor);
		controller = new GUIModelTriggerTreeController(triggerEnvironment, root, ((DefaultTreeModel) getModel()));
		setEditable(true);
		setDragEnabled(true);
		setDropMode(DropMode.INSERT);
		setTransferHandler(new TriggerTreeTransferHandler(controller));

		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteNode");
		getActionMap().put("deleteNode", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (getSelectionCount() == 1) {
					final Object lastPathComponent = getSelectionPath().getLastPathComponent();
					if (lastPathComponent instanceof TriggerTreeNode) {
						controller.deleteTrigger(((TriggerTreeNode) lastPathComponent).getTrigger());
					} else if (lastPathComponent instanceof TriggerCategoryTreeNode) {
						controller.deleteCategory(((TriggerCategoryTreeNode) lastPathComponent).getCategory());
					}
				}
			}
		});
	}

	public GUIModelTriggerTreeController getController() {
		return controller;
	}

	public void select(final TriggerCategory category) {
		final TriggerCategoryTreeNode node = root.getNode(category);
		stopEditing();
		setSelectionPath(new TreePath(new Object[] { root, node }));
	}

	public void select(final Trigger trigger) {
		final TriggerCategoryTreeNode categoryNode = root.getNode(trigger.getCategory());
		final TriggerTreeNode triggerNode = categoryNode.getNode(trigger);
		stopEditing();
		setSelectionPath(new TreePath(new Object[] { root, categoryNode, triggerNode }));
	}

	public class GUIModelTriggerTreeController implements TriggerTreeController {
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
			final TriggerTreeNode triggerNode = root.getNode(trigger.getCategory()) == null ? null
					: root.getNode(trigger.getCategory()).getNode(trigger);
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
			final TreePath selectionPath = getSelectionPath();
			TreePath newSelectionPath = null;
			if (getSelectionCount() == 1) {
				if (selectionPath.getLastPathComponent() == nodeToDelete) {
					final int nextChildIndex = categoryNode.getIndex(nodeToDelete) + 1;
					final int triggersInCategoryCount = categoryNode.getChildCount();
					if (triggersInCategoryCount == 1) {
						newSelectionPath = selectionPath.getParentPath();
					} else {
						newSelectionPath = selectionPath.getParentPath().pathByAddingChild(
								categoryNode.getChildAt(Math.min(nextChildIndex, triggersInCategoryCount - 2)));
					}
				}
			}
			treeModel.removeNodeFromParent(nodeToDelete);
			delegate.deleteTrigger(trigger);
			if (newSelectionPath != null) {
				setSelectionPath(newSelectionPath);
			}
		}

		@Override
		public void deleteCategory(final TriggerCategory category) {
			final TriggerCategoryTreeNode categoryNode = root.getNode(category);
			final TreePath selectionPath = getSelectionPath();
			TreePath newSelectionPath = null;
			if (getSelectionCount() == 1) {
				if (selectionPath.getLastPathComponent() == categoryNode) {
					final int nextChildIndex = root.getIndex(categoryNode) + 1;
					final int categoryCount = root.getChildCount();
					if (categoryCount == 1) {
						newSelectionPath = selectionPath.getParentPath();
					} else {
						newSelectionPath = selectionPath.getParentPath()
								.pathByAddingChild(root.getChildAt(Math.min(nextChildIndex, categoryCount - 2)));
					}
				}
			}
			treeModel.removeNodeFromParent(categoryNode);
			delegate.deleteCategory(category);
			if (newSelectionPath != null) {
				setSelectionPath(newSelectionPath);
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

	public Trigger createTrigger() {
		return createTrigger(TypedTriggerInstantiator.TRIGGER);
	}

	public Trigger createTriggerComment() {
		return createTrigger(TypedTriggerInstantiator.COMMENT);
	}

	private enum TypedTriggerInstantiator {
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
	}

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

	private static final class TriggerTreeTransferHandler extends TransferHandler {
		private final DataFlavor[] nodeFlavors;
		Trigger[] triggersToRemove;
		TriggerCategory[] categoriesToRemove;
		private final TriggerTreeController controller;

		public TriggerTreeTransferHandler(final TriggerTreeController controller) {
			this.controller = controller;
			nodeFlavors = new DataFlavor[1];
			try {
				nodeFlavors[0] = new DataFlavor(
						DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + DraggableNode.class.getName() + "\"");
			} catch (final ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean canImport(final TransferSupport support) {
			if (!support.isDrop()) {
				return false;
			}
			support.setShowDropLocation(true);
			boolean supported = false;
			for (final DataFlavor nodeFlavor : nodeFlavors) {
				if (support.isDataFlavorSupported(nodeFlavor)) {
					supported = true;
				}
			}
			if (!supported) {
				return false;
			}
			// Do not allow a drop on the drag source selections.
			final JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
			final JTree tree = (JTree) support.getComponent();
			final int dropRow = tree.getRowForPath(dl.getPath());
			final int[] selRows = tree.getSelectionRows();
            for (int selRow : selRows) {
                if (selRow == dropRow) {
                    return false;
                }
            }
			// int action = support.getDropAction();
			// if(action == MOVE) {
			// return true;
			// }
			// Do not allow a non-leaf node to be copied to a level which is less than its source level
			// final TreePath dest = dl.getPath();
			// final DefaultMutableTreeNode target = (DefaultMutableTreeNode) dest.getLastPathComponent();
			// final TreePath path = tree.getPathForRow(selRows[0]);
			// final DefaultMutableTreeNode firstNode = (DefaultMutableTreeNode) path.getLastPathComponent();
			// if (firstNode.getChildCount() > 0 && target.getLevel() < firstNode.getLevel()) {
			// return false;
			// }
			return true;
		}

		/** Defensive copy used in createTransferable. */
		private TriggerElementTreeNode copy(final TreeNode node) {
			if (node instanceof TriggerElementTreeNode) {
				return ((TriggerElementTreeNode) node).copy();
			}
			throw new RuntimeException("unable to copy: " + node);
		}

		@Override
		protected Transferable createTransferable(final JComponent c) {
			final JTree tree = (JTree) c;
			if (tree.getSelectionCount() == 1) {
				final TreePath selectionPath = tree.getSelectionPath();
				final Object lastPathComponent = selectionPath.getLastPathComponent();
				if (lastPathComponent instanceof TriggerTreeNode) {
					final TriggerTreeNode triggerTreeNode = (TriggerTreeNode) lastPathComponent;
					triggersToRemove = new Trigger[] { triggerTreeNode.getTrigger() };
					categoriesToRemove = new TriggerCategory[0];
					return new NodesTransferable(
							new DraggableNode[] { new TriggerDraggableNode(triggerTreeNode.getTrigger().copy()) });
				} else if (lastPathComponent instanceof TriggerCategoryTreeNode) {
					final TriggerCategoryTreeNode triggerTreeNode = (TriggerCategoryTreeNode) lastPathComponent;
					triggersToRemove = new Trigger[0];
					categoriesToRemove = new TriggerCategory[] { triggerTreeNode.getCategory() };
					return new NodesTransferable(
							new DraggableNode[] { new CategoryDraggableNode(triggerTreeNode.getCategory().copy()) });
				}
			}
			return null;
		}

		@Override
		protected void exportDone(final JComponent source, final Transferable data, final int action) {
			if ((action & MOVE) == MOVE) {
				// Remove nodes saved by createTransferable.
                for (Trigger trigger : triggersToRemove) {
                    controller.deleteTrigger(trigger);
                }
                for (TriggerCategory triggerCategory : categoriesToRemove) {
                    controller.deleteCategory(triggerCategory);
                }
			}
		}

		@Override
		public int getSourceActions(final JComponent c) {
			return COPY_OR_MOVE;
		}

		@Override
		public boolean importData(final TransferHandler.TransferSupport support) {
			if (!canImport(support)) {
				return false;
			}
			// Extract transfer data.
			DraggableNode[] triggers = null;
			try {
				final Transferable t = support.getTransferable();
				final DataFlavor[] transferDataFlavors = t.getTransferDataFlavors();
				triggers = (DraggableNode[]) t.getTransferData(nodeFlavors[0]);
				System.out.println(Arrays.toString(transferDataFlavors));
			} catch (final UnsupportedFlavorException ufe) {
				System.out.println("UnsupportedFlavor: " + ufe.getMessage());
			} catch (final java.io.IOException ioe) {
				System.out.println("I/O error: " + ioe.getMessage());
			}
			// Get drop location info.
			final JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
			final int childIndex = dl.getChildIndex();
			final TreePath dest = dl.getPath();
			final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dest.getLastPathComponent();
			final JTree tree = (JTree) support.getComponent();
			final DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			// Configure for drop mode.
			int index = childIndex; // DropMode.INSERT
			if (childIndex == -1) { // DropMode.ON
				index = parent.getChildCount();
			}
			// Add data to model.
			if (triggers != null) {
                for (DraggableNode trigger : triggers) {
                    trigger.dragInto(controller, parent, index++);
                }
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			return getClass().getName();
		}

		public class NodesTransferable implements Transferable {
			DraggableNode[] nodes;

			public NodesTransferable(final DraggableNode[] nodes) {
				this.nodes = nodes;
			}

			@Override
			public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException {
				if (!isDataFlavorSupported(flavor)) {
					throw new UnsupportedFlavorException(flavor);
				}
				return nodes;
			}

			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return nodeFlavors;
			}

			@Override
			public boolean isDataFlavorSupported(final DataFlavor flavor) {
				return nodeFlavors[0].equals(flavor);
			}
		}

		private interface DraggableNode {
			void dragInto(TriggerTreeController controller, DefaultMutableTreeNode rootGuy, int index);
		}

		private static final class TriggerDraggableNode implements DraggableNode, Serializable {
			/**
			 *
			 */
			private static final long serialVersionUID = -3149168228812835946L;
			private final Trigger trigger;

			public TriggerDraggableNode(final Trigger trigger) {
				this.trigger = trigger;
			}

			public Trigger getTrigger() {
				return trigger;
			}

			@Override
			public void dragInto(final TriggerTreeController controller, final DefaultMutableTreeNode rootGuy,
					final int index) {
				controller.moveTrigger(trigger, ((TriggerCategoryTreeNode) rootGuy).getCategory(), index);
			}

		}

		private static final class CategoryDraggableNode implements DraggableNode, Serializable {
			/**
			 *
			 */
			private static final long serialVersionUID = -2892510828683155370L;
			private final TriggerCategory category;

			public CategoryDraggableNode(final TriggerCategory category) {
				this.category = category;
			}

			public TriggerCategory getCategory() {
				return category;
			}

			@Override
			public void dragInto(final TriggerTreeController controller, final DefaultMutableTreeNode rootGuy,
					final int index) {
				controller.moveCategory(category, index);
			}

		}
	}
}