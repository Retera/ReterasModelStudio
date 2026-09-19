package com.hiveworkshop.wc3.gui.modeledit.componenttree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import com.hiveworkshop.wc3.gui.modeledit.ModelComponentBrowserTree;
import com.hiveworkshop.wc3.mdl.IdObject;

/**
 * Drag a node row onto another node to make it that node's child, or onto the
 * "Nodes" group to make it a root. Backed by the same undoable reparent as
 * the Move Left / Move Right menu items.
 */
public final class NodeReparentTransferHandler extends TransferHandler {
	private static final DataFlavor NODE_FLAVOR = new DataFlavor(IdObject.class, "Model node");

	private final ModelComponentBrowserTree tree;
	private final ModelComponentTreeController controller;

	public NodeReparentTransferHandler(final ModelComponentBrowserTree tree,
			final ModelComponentTreeController controller) {
		this.tree = tree;
		this.controller = controller;
	}

	@Override
	public int getSourceActions(final JComponent c) {
		return MOVE;
	}

	@Override
	protected Transferable createTransferable(final JComponent c) {
		final ComponentRef ref = tree.getSelectedRef();
		if (!ref.isNode()) {
			return null;
		}
		final IdObject node = ref.asNode();
		return new Transferable() {
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] { NODE_FLAVOR };
			}

			@Override
			public boolean isDataFlavorSupported(final DataFlavor flavor) {
				return NODE_FLAVOR.equals(flavor);
			}

			@Override
			public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException {
				if (!isDataFlavorSupported(flavor)) {
					throw new UnsupportedFlavorException(flavor);
				}
				return node;
			}
		};
	}

	private IdObject draggedNode(final TransferSupport support) {
		try {
			return (IdObject) support.getTransferable().getTransferData(NODE_FLAVOR);
		} catch (final Exception e) {
			return null;
		}
	}

	/** @return the target parent, or null for "make root"; NOT_A_TARGET if invalid */
	private static final Object NOT_A_TARGET = new Object();

	private Object dropTarget(final TransferSupport support) {
		if (!(support.getDropLocation() instanceof JTree.DropLocation)) {
			return NOT_A_TARGET;
		}
		final TreePath path = ((JTree.DropLocation) support.getDropLocation()).getPath();
		if (path == null) {
			return NOT_A_TARGET;
		}
		final ComponentRef ref = tree.refAt(path);
		if (ref.isNode()) {
			return ref.asNode();
		}
		if (ref.isGroup() && ref.isInNodesGroup()) {
			return null;
		}
		return NOT_A_TARGET;
	}

	@Override
	public boolean canImport(final TransferSupport support) {
		if (!support.isDrop() || !support.isDataFlavorSupported(NODE_FLAVOR)) {
			return false;
		}
		final Object target = dropTarget(support);
		if (target == NOT_A_TARGET) {
			return false;
		}
		final IdObject dragged = draggedNode(support);
		return controller.canReparent(dragged, (IdObject) target);
	}

	@Override
	public boolean importData(final TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}
		final IdObject dragged = draggedNode(support);
		final Object target = dropTarget(support);
		controller.reparent(dragged, (IdObject) target);
		return true;
	}

	@Override
	protected void exportDone(final JComponent source, final Transferable data, final int action) {
		// reparenting happened in importData; nothing to clean up
	}
}
