package com.hiveworkshop.rms.ui.gui.modeledit.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashSet;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.EventListenerList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * This code is taken from a Stack Overflow post:
 * https://stackoverflow.com/questions/21847411/java-swing-need-a-good-quality-developed-jtree-with-checkboxes
 *
 * Thanks to the creators!
 *
 */
public class JDoubleCheckBoxTree extends JTree {

	private static final long serialVersionUID = -4194122328392241790L;

	// Defining data structure that will enable to fast check-indicate the state
	// of each node
	// It totally replaces the "selection" mechanism of the JTree
	private static class CheckedNode {
		boolean isSelected1;
		boolean isSelected2;
		boolean hasChildren;
		boolean allChildrenSelected;

		public CheckedNode(final boolean isSelected1_, final boolean isSelected2_, final boolean hasChildren_,
				final boolean allChildrenSelected_) {
			isSelected1 = isSelected1_;
			isSelected2 = isSelected2_;
			hasChildren = hasChildren_;
			allChildrenSelected = allChildrenSelected_;
		}
	}

	HashSet<TreeNode> checkedPaths = new HashSet<>();
	HashSet<TreeNode> checkedPaths2 = new HashSet<>();

	// Defining a new event type for the checking mechanism and preparing
	// event-handling mechanism
	protected EventListenerList listenerList = new EventListenerList();

	public static class CheckChangeEvent extends EventObject {
		private static final long serialVersionUID = -8100230309044193368L;
		private final TreePath treePath;

		public CheckChangeEvent(final Object source, final TreePath path) {
			super(source);
			this.treePath = path;
		}

		public TreePath getTreePath() {
			return treePath;
		}
	}

	public interface CheckChangeEventListener extends EventListener {
		void checkStateChanged(CheckChangeEvent event);
	}

	public void addCheckChangeEventListener(final CheckChangeEventListener listener) {
		listenerList.add(CheckChangeEventListener.class, listener);
	}

	public void removeCheckChangeEventListener(final CheckChangeEventListener listener) {
		listenerList.remove(CheckChangeEventListener.class, listener);
	}

	void fireCheckChangeEvent(final CheckChangeEvent evt) {
		final Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CheckChangeEventListener.class) {
				((CheckChangeEventListener) listeners[i + 1]).checkStateChanged(evt);
			}
		}
	}

	// Override
	@Override
	public void setModel(final TreeModel newModel) {
		super.setModel(newModel);
		resetCheckingState();
	}

	// New method that returns only the checked paths (totally ignores original
	// "selection" mechanism)
	public TreeNode[] getCheckedPaths() {
		return checkedPaths.toArray(new TreeNode[0]);
	}

	// New method that returns only the checked paths (totally ignores original
	// "selection" mechanism)
	public TreeNode[] getCheckedPaths2() {
		return checkedPaths2.toArray(new TreeNode[0]);
	}

	// Returns true in case that the node is selected, has children but not all
	// of them are selected
	public boolean isSelected2Partially(final JDoubleCheckBoxTreeNode node) {
		return node.isChecked2() && node.getChildCount() > 0 && !node.isAllChildrenV2Selected();
	}

	// Returns true in case that the node is selected, has children but not all
	// of them are selected
	public boolean isSelectedPartially(final JDoubleCheckBoxTreeNode node) {
		return node.isChecked() && node.getChildCount() > 0 && !node.isAllChildrenSelected();
	}

	public boolean isSelected(final JDoubleCheckBoxTreeNode node) {
		return node.isChecked();
	}

	public boolean isSelected2(final JDoubleCheckBoxTreeNode node) {
		return node.isChecked2();
	}

	/**
	 * Reloads the checking state from the data model.
	 */
	private void resetCheckingState() {
		checkedPaths = new HashSet<>();
		final JDoubleCheckBoxTreeNode node = (JDoubleCheckBoxTreeNode) getModel().getRoot();
		if (node == null) {
			return;
		}
		checkAllCheckedRecursively(node);
	}

	/**
	 * Reloads the checking state from the data model.
	 */
	private void resetCheckingV2State() {
		checkedPaths2 = new HashSet<>();
		final JDoubleCheckBoxTreeNode node = (JDoubleCheckBoxTreeNode) getModel().getRoot();
		if (node == null) {
			return;
		}
		checkAllCheckedV2Recursively(node);
	}

	// Creating data structure of the current model for the checking mechanism
	private boolean checkAllCheckedRecursively(final JDoubleCheckBoxTreeNode node) {
		final TreeNode[] path = node.getPath();
		final TreePath tp = new TreePath(path);
		boolean allChildrenChecked = true;
		boolean anyChildChecked = false;
		for (int i = 0; i < node.getChildCount(); i++) {
			final TreeNode childAt = node.getChildAt(i);
			if (checkAllCheckedRecursively((JDoubleCheckBoxTreeNode) tp.pathByAddingChild(childAt).getLastPathComponent())) {
			} else {
				allChildrenChecked = false;
			}
			if (((JDoubleCheckBoxTreeNode) childAt).isChecked()) {
				anyChildChecked = true;
			}

		}
		if (node.getChildCount() > 0 && anyChildChecked && !node.isHasPersonalState()) {
			node.setChecked(true);
		}
		if (!node.isHasPersonalState()) {
			node.setAllChildrenSelected(allChildrenChecked);
		}
		return allChildrenChecked && node.isChecked();
	}

	// Creating data structure of the current model for the checking mechanism
	private boolean checkAllCheckedV2Recursively(final JDoubleCheckBoxTreeNode node) {
		final TreeNode[] path = node.getPath();
		final TreePath tp = new TreePath(path);
		boolean allChildrenChecked = true;
		boolean anyChildChecked = false;
		for (int i = 0; i < node.getChildCount(); i++) {
			final TreeNode childAt = node.getChildAt(i);
			if (checkAllCheckedV2Recursively((JDoubleCheckBoxTreeNode) tp.pathByAddingChild(childAt).getLastPathComponent())) {
			} else {
				allChildrenChecked = false;
			}
			if (((JDoubleCheckBoxTreeNode) childAt).isChecked2()) {
				anyChildChecked = true;
			}

		}
		if (node.getChildCount() > 0 && anyChildChecked && !node.isHasPersonalState2()) {
			node.setChecked2(true);
		}
		if (!node.isHasPersonalState2()) {
			node.setAllChildrenV2Selected(allChildrenChecked);
		}
		return allChildrenChecked && node.isChecked2();
	}

	// Overriding cell renderer by a class that ignores the original "selection"
	// mechanism
	// It decides how to show the nodes due to the checking-mechanism
	private static class CheckBoxCellRenderer extends JPanel implements TreeCellRenderer {
		private static final long serialVersionUID = -7341833835878991719L;
		JCheckBox checkBox;

		public CheckBoxCellRenderer() {
			super();
			this.setLayout(new BorderLayout());
			checkBox = new JCheckBox();
			add(checkBox, BorderLayout.CENTER);
			setOpaque(false);
		}

		@Override
		public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected,
				final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
			final JDoubleCheckBoxTreeNode node = (JDoubleCheckBoxTreeNode) value;
			final Object obj = node.getUserObject();
			checkBox.setSelected(node.isChecked());
			checkBox.setText(obj.toString());
			checkBox.setOpaque(node.isChecked() && node.getChildCount() > 0 && !node.isAllChildrenSelected());
			return this;
		}
	}

	public JDoubleCheckBoxTree(final TreeModel treeModel) {
		super(treeModel);
		// Disabling toggling by double-click
		this.setToggleClickCount(0);
		// Overriding cell renderer by new one defined above
		final CheckBoxCellRenderer cellRenderer = new CheckBoxCellRenderer();
		this.setCellRenderer(cellRenderer);

		// Overriding selection model by an empty one
		final DefaultTreeSelectionModel dtsm = new DefaultTreeSelectionModel() {
			private static final long serialVersionUID = -8190634240451667286L;

			// Totally disabling the selection mechanism
			@Override
			public void setSelectionPath(final TreePath path) {
			}

			@Override
			public void addSelectionPath(final TreePath path) {
			}

			@Override
			public void removeSelectionPath(final TreePath path) {
			}

			@Override
			public void setSelectionPaths(final TreePath[] pPaths) {
			}
		};
		// Calling checking mechanism on mouse click
		this.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(final MouseEvent arg0) {
			}

			@Override
			public void mouseEntered(final MouseEvent arg0) {
			}

			@Override
			public void mouseExited(final MouseEvent arg0) {
			}

			@Override
			public void mousePressed(final MouseEvent arg0) {
			}

			@Override
			public void mouseReleased(final MouseEvent arg0) {
				final TreePath tp = JDoubleCheckBoxTree.this.getPathForLocation(arg0.getX(), arg0.getY());
				if (tp == null) {
					return;
				}
				final boolean checkMode = !((JDoubleCheckBoxTreeNode) tp.getLastPathComponent()).isChecked();
				checkSubTree(tp, checkMode);
				updatePredecessorsWithCheckMode(tp, checkMode);
				// Firing the check change event
				fireCheckChangeEvent(new CheckChangeEvent(tp.getLastPathComponent(), tp));
				// Repainting tree after the data structures were updated
				JDoubleCheckBoxTree.this.repaint();
			}
		});
		this.setSelectionModel(dtsm);
	}

	// When a node is checked/unchecked, updating the states of the predecessors
	protected void updatePredecessorsWithCheckMode(final TreePath tp, final boolean check) {
		final TreePath parentPath = tp.getParentPath();
		// If it is the root, stop the recursive calls and return
		if (parentPath == null) {
			return;
		}
		final JDoubleCheckBoxTreeNode parentCheckedNode = (JDoubleCheckBoxTreeNode) parentPath.getLastPathComponent();
		final DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
		parentCheckedNode.setAllChildrenSelected(true);
		parentCheckedNode.setChecked(false);
		for (int i = 0; i < parentNode.getChildCount(); i++) {
			final TreePath childPath = parentPath.pathByAddingChild(parentNode.getChildAt(i));
			final JDoubleCheckBoxTreeNode childCheckedNode = (JDoubleCheckBoxTreeNode) (childPath.getLastPathComponent());
			// It is enough that even one subtree is not fully selected
			// to determine that the parent is not fully selected
			if (!childCheckedNode.isAllChildrenSelected()) {
				parentCheckedNode.setAllChildrenSelected(false);
			}
			// If at least one child is selected, selecting also the parent
			if (childCheckedNode.isChecked() && !parentCheckedNode.isHasPersonalState()) {
				parentCheckedNode.setChecked(true);
			}
		}
		if (parentCheckedNode.isChecked()) {
			checkedPaths.add((TreeNode) parentPath.getLastPathComponent());
		} else {
			checkedPaths.remove(parentPath.getLastPathComponent());
		}
		// Go to upper predecessor
		updatePredecessorsWithCheckMode(parentPath, check);
	}

	// Recursively checks/unchecks a subtree
	protected void checkSubTree(final TreePath tp, final boolean check) {
		final JDoubleCheckBoxTreeNode cn = (JDoubleCheckBoxTreeNode) (tp.getLastPathComponent());
		cn.setChecked(check);
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();
		if (!cn.isHasPersonalState()) {
			for (int i = 0; i < node.getChildCount(); i++) {
				checkSubTree(tp.pathByAddingChild(node.getChildAt(i)), check);
			}
			cn.setAllChildrenSelected(check);
		}
		if (check) {
			checkedPaths.add((TreeNode) tp.getLastPathComponent());
		} else {
			checkedPaths.remove(tp.getLastPathComponent());
		}
	}

}