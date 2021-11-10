package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui;

import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.Trigger;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.TriggerCategory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Arrays;

final class TriggerTreeTransferHandler extends TransferHandler {
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

	/**
	 * Defensive copy used in createTransferable.
	 */
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
				triggersToRemove = new Trigger[] {triggerTreeNode.getTrigger()};
				categoriesToRemove = new TriggerCategory[0];
				return new NodesTransferable(
						new DraggableNode[] {new TriggerDraggableNode(triggerTreeNode.getTrigger().copy())});
			} else if (lastPathComponent instanceof TriggerCategoryTreeNode) {
				final TriggerCategoryTreeNode triggerTreeNode = (TriggerCategoryTreeNode) lastPathComponent;
				triggersToRemove = new Trigger[0];
				categoriesToRemove = new TriggerCategory[] {triggerTreeNode.getCategory()};
				return new NodesTransferable(
						new DraggableNode[] {new CategoryDraggableNode(triggerTreeNode.getCategory().copy())});
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
	public boolean importData(final TransferSupport support) {
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

}
