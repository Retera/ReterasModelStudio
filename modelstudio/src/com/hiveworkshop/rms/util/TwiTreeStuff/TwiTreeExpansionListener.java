package com.hiveworkshop.rms.util.TwiTreeStuff;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class TwiTreeExpansionListener implements TreeExpansionListener {
	private final Set<TreePath> expandedPaths = new HashSet<>();
	private final Set<TreePath> collapsedPaths = new HashSet<>();
	private boolean expansionPropagateKeyDown = false;
	private boolean isPropagating = false;

	public TwiTreeExpansionListener setExpansionPropagateKeyDown(boolean expansionPropagateKeyDown) {
		this.expansionPropagateKeyDown = expansionPropagateKeyDown;
		return this;
	}

	public void clear() {
		expandedPaths.clear();
		collapsedPaths.clear();
		expansionPropagateKeyDown = false;
		isPropagating = false;
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		expandedPaths.add(event.getPath());
		collapsedPaths.remove(event.getPath());
//		System.out.println("treeExpanded, path: " + event.getPath());

		propagateExpansion(event, true);
	}

	private void printEventInfo(TreeExpansionEvent event) {
//		System.out.println("source#getCursor:         " + ((ComponentThingTree) event.getSource()).getCursor());
//		System.out.println("source#getComponents:         " + ((ComponentThingTree) event.getSource()).getParent());
//		System.out.println("source#getComponents:         " + Arrays.toString(((JViewport) ((ComponentThingTree) event.getSource()).getParent()).getComponents()));
//		System.out.println("source#getComponents:         " + ((JViewport)((ComponentThingTree) event.getSource()).getParent()).getView());
////					System.out.println("source#getComponents:         " + ((JViewport)((ComponentThingTree) event.getSource()).getParent()).getComponents());
//		System.out.println("source#getComponents:         " + Arrays.toString(((ComponentThingTree) event.getSource()).getComponents()));
//		System.out.println("source#getComponents:         " + Arrays.toString(((CellRendererPane) ((ComponentThingTree) event.getSource()).getComponents()[0]).getComponents()));
//		System.out.println("source#getComponents:         " + Arrays.toString(((JPanel) ((CellRendererPane) ((ComponentThingTree) event.getSource()).getComponents()[0]).getComponents()[0]).getComponents()));
//		System.out.println("source#getComponents:         " + ((JPanel)((CellRendererPane)((ComponentThingTree) event.getSource()).getComponents()[0]).getComponents()[0]).getComponents());
//		System.out.println("source#getMousePosition:  " + ((ComponentThingTree) event.getSource()).getMousePosition(true));
//		System.out.println("source#getClientProperty: " + ((ComponentThingTree) event.getSource()).getClientProperty("flags"));
//		System.out.println("source#getActionMap:      " + ((ComponentThingTree) event.getSource()).getActionMap());
//		System.out.println("source#getInputMap:       " + ((ComponentThingTree) event.getSource()).getInputMap());
//		System.out.println("source#getEditingPath:          " + ((ComponentThingTree) event.getSource()).getEditingPath());
//		System.out.println("source#getModel:                " + ((ComponentThingTree) event.getSource()).getModel());
//		System.out.println("source#getRegisteredKeyStrokes: " + Arrays.toString(((ComponentThingTree) event.getSource()).getRegisteredKeyStrokes()));
//		System.out.println("source#getInputMethodRequests:  " + ((ComponentThingTree) event.getSource()).getInputMethodRequests());
////					System.out.println("source#: " + ((ComponentThingTree)event.getSource()).keyDown(event, KeyEvent.VK_CONTROL));
//		System.out.println("source#getKeyAdapter: " + ((ComponentThingTree) event.getSource()).getKeyAdapter());
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		expandedPaths.remove(event.getPath());
		collapsedPaths.add(event.getPath());
//		System.out.println("treeCollapsed, path: " + event.getPath());
		propagateExpansion(event, false);
	}

	private void propagateExpansion(TreeExpansionEvent event, boolean expand) {
		if (expansionPropagateKeyDown && (!isPropagating)) {
			isPropagating = true;
			Object source = event.getSource();
			TreePath path = event.getPath();
			if (path.getLastPathComponent() instanceof TreeNode) {
				TreeNode lastPathComponent = (TreeNode) path.getLastPathComponent();
				System.out.println("Expanding tree");
				long l = System.currentTimeMillis();
				expandAllChildren(source, lastPathComponent, path, expand);
				System.out.println("\ttook: " + (System.currentTimeMillis() - l) + " ms");
			}
			isPropagating = false;
		}
	}


	private void expandAllChildren(Object source, TreeNode node, TreePath path, boolean expand) {
		Set<TreePath> childrenToExpand = getChildrenToExpand(node, path);
		if (source instanceof JTree) {
			if (expand) {
//				System.out.println("expanding " + childrenToExpand.size() + " treePaths");
				for (TreePath treePath : childrenToExpand) {
					((JTree)source).expandPath(treePath);
				}
			} else {
//				System.out.println("collapsing " + childrenToExpand.size() + " treePaths");
				for (TreePath treePath : childrenToExpand) {
					((JTree)source).collapsePath(treePath);
				}
			}
		}
	}
	private void expandAllChildren1(Object source, TreeNode node, TreePath path, boolean expand) {
		for (int i = 0; i < node.getChildCount(); i++) {
			TreeNode child = node.getChildAt(i);
			if (!child.isLeaf()) {
				expandAllChildren1(source, child, path.pathByAddingChild(child), expand);
			}
		}
		if (source instanceof JTree) {
			if (expand) {
				((JTree)source).expandPath(path);
			} else {
				((JTree)source).collapsePath(path);
			}
		}
	}


	private final Set<TreePath> pathsToExpand = new LinkedHashSet<>();
	private Set<TreePath> getChildrenToExpand(TreeNode node, TreePath path) {
		pathsToExpand.clear();
		collectChildrenToExpand(node, path, pathsToExpand);
		return pathsToExpand;
	}
	private void collectChildrenToExpand(TreeNode node, TreePath path, Set<TreePath> pathsToExpand) {
		for (int i = 0; i < node.getChildCount(); i++) {
			TreeNode child = node.getChildAt(i);
			if (!child.isLeaf()) {
				collectChildrenToExpand(child, path.pathByAddingChild(child), pathsToExpand);
			}
		}
		pathsToExpand.add(path);
	}


	public void openTree(JTree tree) {
		if (!expandedPaths.isEmpty()) {
			isPropagating = true;
			for (TreePath path : collapsedPaths) {
				expandedPaths.removeIf(path::isDescendant);
			}
			collapsedPaths.clear();

			for (TreePath treePath : expandedPaths) {
				try {
					tree.makeVisible(treePath);
					if (tree.isVisible(treePath) && tree.isCollapsed(treePath)) {
						tree.expandPath(treePath);
					}
				} catch (Exception e) {
					System.out.println("faild on: " + treePath);
					e.printStackTrace();
				}
			}
			isPropagating = false;
		}
	}
}
