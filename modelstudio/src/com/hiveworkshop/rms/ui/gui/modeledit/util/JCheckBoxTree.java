package com.hiveworkshop.rms.ui.gui.modeledit.util;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashSet;

/**
 * This code is taken from a Stack Overflow post:
 * https://stackoverflow.com/questions/21847411/java-swing-need-a-good-quality-developed-jtree-with-checkboxes
 *
 * Thanks to the creators!
 *
 */
public class JCheckBoxTree extends JTree {

	private static final long serialVersionUID = -4194122328392241790L;

	// Defining a new event type for the checking mechanism and preparing event-handling mechanism
	protected EventListenerList listenerList = new EventListenerList();

	HashSet<TreeNode> checkedPaths = new HashSet<>();
	private boolean controlDown = false;

	public JCheckBoxTree() {
		super();
		// Disabling toggling by double-click
		setToggleClickCount(0);
		setOpaque(false);

		// Overriding cell renderer by new one defined above
		CheckBoxCellRenderer cellRenderer = new CheckBoxCellRenderer();
		setCellRenderer(cellRenderer);

		// Overriding selection model by an empty one
		DefaultTreeSelectionModel dtsm = getDisabledSelectionModel();
		setSelectionModel(dtsm);

		addMouseListener(getMouseListener());
		addKeyListener(getKeyAdapter());
		addTreeExpansionListener(getExpansionListener());
	}

	private TreeExpansionListener getExpansionListener() {
		return new TreeExpansionListener() {
			boolean isSt = false;

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				if (controlDown && (!isSt)) {
					isSt = true;
					if (event.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode) {
						DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
						expandAllChildren(lastPathComponent, event.getPath(), true);
					}
					isSt = false;
				}
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				if (controlDown && (!isSt)) {
					isSt = true;
					if (event.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode) {
						DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
						expandAllChildren(lastPathComponent, event.getPath(), false);
					}
					isSt = false;
				}
			}
		};
	}

	private void expandAllChildren(TreeNode node, TreePath path, boolean expand) {
		for (int i = 0; i < node.getChildCount(); i++) {
			TreeNode child = node.getChildAt(i);
			expandAllChildren(child, path.pathByAddingChild(child), expand);
		}
		if (expand) {
			expandPath(path);
		} else {
			collapsePath(path);
		}
	}

	private DefaultTreeSelectionModel getDisabledSelectionModel() {
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
		return dtsm;
	}

	private MouseListener getMouseListener() {
		// Calling checking mechanism on mouse click
		return new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
			}

			@Override
			public void mouseEntered(final MouseEvent e) {
			}

			@Override
			public void mouseExited(final MouseEvent e) {
			}

			@Override
			public void mousePressed(final MouseEvent e) {
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				System.out.println("mouse released over : " + JCheckBoxTree.this.getPathForLocation(e.getX(), e.getY()));
				if (!SwingUtilities.isRightMouseButton(e)) {
					TreePath tp = JCheckBoxTree.this.getPathForLocation(e.getX(), e.getY());
					if (tp == null) {
						return;
					}
					boolean checkMode = !((JCheckBoxTreeNode) tp.getLastPathComponent()).isChecked();
					if (e.isShiftDown()) {
						checkSubTree(tp, -1, checkMode);
//						checkSubTree(tp, checkMode);
					} else {
						checkSubTree(tp, 0, checkMode);
					}
					updatePredecessorsWithCheckMode(tp, checkMode);
					// Firing the check change event
					fireCheckChangeEvent(new CheckChangeEvent(tp.getLastPathComponent(), tp));
					// Repainting tree after the data structures were updated
					JCheckBoxTree.this.repaint();
				}
			}
		};
	}

	private KeyAdapter getKeyAdapter() {
		return new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTROL && !controlDown) System.out.println("controll down");
				controlDown = e.isControlDown();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				controlDown = e.isControlDown();
				if (e.getKeyCode() == KeyEvent.VK_CONTROL) System.out.println("controll up");
			}
		};
	}

	void fireCheckChangeEvent(final CheckChangeEvent evt) {
		final Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CheckChangeEventListener.class) {
				((CheckChangeEventListener) listeners[i + 1]).checkStateChanged(evt);
			}
		}
	}

	public void updateModel(final TreeModel newModel) {
		super.setModel(newModel);
		resetCheckingState();
	}

	// New method that returns only the checked paths (totally ignores original "selection" mechanism)
	public TreeNode[] getCheckedPaths() {
		return checkedPaths.toArray(new TreeNode[0]);
	}

	public boolean isSelected(final JCheckBoxTreeNode node) {
		return node.isChecked();
	}

	/**
	 * Reloads the checking state from the data model.
	 */
	private void resetCheckingState() {
		System.out.println("resetCheckingState");
		checkedPaths = new HashSet<>();
		final JCheckBoxTreeNode node = (JCheckBoxTreeNode) getModel().getRoot();
		if (node == null) {
			return;
		}
		updateAllNodeStates(node);
	}

	// Creating data structure of the current model for the checking mechanism
	private JCheckBoxTreeNode.ChildrenSelected updateAllNodeStates(final JCheckBoxTreeNode node) {
		final TreePath tp = new TreePath(node.getPath());

		boolean foundChecked = false;
		boolean foundUnchecked = false;
		JCheckBoxTreeNode.ChildrenSelected childState = JCheckBoxTreeNode.ChildrenSelected.ALL;

		for (int i = 0; i < node.getChildCount(); i++) {
			final TreeNode childAt = node.getChildAt(i);
			Object lastPathComponent = tp.pathByAddingChild(childAt).getLastPathComponent();

			JCheckBoxTreeNode child = (JCheckBoxTreeNode) lastPathComponent;
			JCheckBoxTreeNode.ChildrenSelected childrenSelected = updateAllNodeStates(child);

			if (child.isChecked() || childrenSelected == JCheckBoxTreeNode.ChildrenSelected.ALL) {
				foundChecked = true;
			} else if (!child.isChecked() || childrenSelected == JCheckBoxTreeNode.ChildrenSelected.NONE) {
				foundUnchecked = true;
			}
			if (childrenSelected == JCheckBoxTreeNode.ChildrenSelected.SOME || foundChecked && foundUnchecked) {
				childState = JCheckBoxTreeNode.ChildrenSelected.SOME;
				break;
			}
		}
		if (foundChecked && !foundUnchecked) {
			childState = JCheckBoxTreeNode.ChildrenSelected.ALL;
		} else if (!foundChecked && foundUnchecked) {
			childState = JCheckBoxTreeNode.ChildrenSelected.NONE;
		}

		node.setChildState(childState);

		return childState;
	}

	protected void checkSubTree(final TreePath tp, int depth, final boolean check) {
		final JCheckBoxTreeNode cn = (JCheckBoxTreeNode) (tp.getLastPathComponent());
		cn.setChecked(check);
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();

		if (depth == -1) {
			depth = node.getDepth() + 1;
		}

		int thisDepth = depth - 1;
		for (int i = 0; i < node.getChildCount() && depth > 0; i++) {
			checkSubTree(tp.pathByAddingChild(node.getChildAt(i)), thisDepth, check);
		}

		if (check) {
			checkedPaths.add(cn);
		} else {
			checkedPaths.remove(cn);
		}
	}

	// When a node is checked/unchecked, updating the states of the predecessors
	protected void updatePredecessorsWithCheckMode(final TreePath tp, final boolean check) {
		final TreePath parentPath = tp.getParentPath();
		// If it is the root, stop the recursive calls and return
		if (parentPath == null) {
			return;
		}
		final JCheckBoxTreeNode parentCheckedNode = (JCheckBoxTreeNode) parentPath.getLastPathComponent();

//		parentCheckedNode.setAllChildrenSelected(true);
		boolean foundChecked = false;
		boolean foundUnchecked = false;

		for (int i = 0; i < parentCheckedNode.getChildCount(); i++) {
			final TreePath childPath = parentPath.pathByAddingChild(parentCheckedNode.getChildAt(i));
			final JCheckBoxTreeNode childCheckedNode = (JCheckBoxTreeNode) (childPath.getLastPathComponent());
			// It is enough that even one subtree is not fully selected to determine that the parent is not fully selected
			if (childCheckedNode.getChildState() == JCheckBoxTreeNode.ChildrenSelected.SOME) {
				foundChecked = true;
				foundUnchecked = true;
			}

			if (childCheckedNode.isChecked() || childCheckedNode.getChildState() == JCheckBoxTreeNode.ChildrenSelected.ALL) {
				foundChecked = true;
			}
			if (!childCheckedNode.isChecked() || childCheckedNode.getChildState() == JCheckBoxTreeNode.ChildrenSelected.NONE) {
				foundUnchecked = true;
			}
			if (foundChecked && foundUnchecked) {
				parentCheckedNode.setChildState(JCheckBoxTreeNode.ChildrenSelected.SOME);
				break;
			}
		}

		if (foundChecked && !foundUnchecked) {
			parentCheckedNode.setChildState(JCheckBoxTreeNode.ChildrenSelected.ALL);
		}
		if (!foundChecked && foundUnchecked) {
			parentCheckedNode.setChildState(JCheckBoxTreeNode.ChildrenSelected.NONE);
		}
		if (parentCheckedNode.isChecked()) {
			checkedPaths.add((TreeNode) parentPath.getLastPathComponent());
		} else {
			checkedPaths.remove((TreeNode) parentPath.getLastPathComponent());
		}
		// Go to upper predecessor
		updatePredecessorsWithCheckMode(parentPath, check);
	}

	// Recursively checks/unchecks a subtree
	protected void checkSubTree(final TreePath tp, final boolean check) {
		final JCheckBoxTreeNode cn = (JCheckBoxTreeNode) (tp.getLastPathComponent());
		cn.setChecked(check);

		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();
		for (int i = 0; i < node.getChildCount(); i++) {
			checkSubTree(tp.pathByAddingChild(node.getChildAt(i)), check);
		}
		cn.setAllChildrenSelected(check);

		if (check) {
			checkedPaths.add((TreeNode) tp.getLastPathComponent());
		} else {
			checkedPaths.remove(tp.getLastPathComponent());
		}
	}

	public interface CheckChangeEventListener extends EventListener {
		void checkStateChanged(CheckChangeEvent event);
	}

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

	// Overriding cell renderer by a class that ignores the original "selection" mechanism
	// It decides how to show the nodes due to the checking-mechanism
	private static class CheckBoxCellRenderer extends JPanel implements TreeCellRenderer {
		private static final long serialVersionUID = -7341833835878991719L;
		JCheckBox checkBox;
		JLabel label;

		public CheckBoxCellRenderer() {
			super();
			this.setLayout(new BorderLayout());
			checkBox = new JCheckBox();
			label = new JLabel();
			add(checkBox, BorderLayout.CENTER);
			add(label, BorderLayout.EAST);
			setOpaque(false);
		}

		@Override
		public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected,
		                                              final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
			if (value instanceof JCheckBoxTreeNode) {
				final JCheckBoxTreeNode node = (JCheckBoxTreeNode) value;
				final Object obj = node.getUserObject();
				checkBox.setSelected(node.isChecked());
				label.setText(obj.toString());
//				checkBox.setText(obj.toString());

				// This is not working as intended (the render update seems to lag in a lot of instances)
//				if (node.getChildState() == JCheckBoxTreeNode.ChildrenSelected.ALL
//						|| node.getChildState() == JCheckBoxTreeNode.ChildrenSelected.NONE && !checkBox.isSelected()){
//					label.setOpaque(false);
//					checkBox.setBackground(new Color(255, 255, 255,0));
//				} else if (node.getChildState() == JCheckBoxTreeNode.ChildrenSelected.SOME){
//					checkBox.setOpaque(true);
//					checkBox.setBackground(new Color(0, 0, 0,15));
//				} else {
//					checkBox.setOpaque(true);
//					checkBox.setBackground(new Color(255, 255, 255,40));
//				}

			}
			return this;
		}
	}

}