package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class ModelTreeExpansionListener implements TreeExpansionListener {
	private boolean controlDown = false;
	private boolean isPropagating = false;

	public ModelTreeExpansionListener setControlDown(boolean controlDown) {
		this.controlDown = controlDown;
		return this;
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		System.out.println("[expand] event: " + event);
		System.out.println("source: " + event.getSource());

//		if (event.getSource() instanceof ComponentThingTree) {
//			printEventInfo(event);
//		}

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
		System.out.println("[collapse] event: " + event);
		propagateExpansion(event, false);
	}

	private void propagateExpansion(TreeExpansionEvent event, boolean expand) {
		if (controlDown && (!isPropagating)) {
			isPropagating = true;
			System.out.println("Control was down!");
			Object source = event.getSource();
			TreePath path = event.getPath();
			if (path.getLastPathComponent() instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) path.getLastPathComponent();
				expandAllChildren(source, lastPathComponent, path, expand);
			}
			isPropagating = false;
		}
	}


	private void expandAllChildren(Object source, TreeNode node, TreePath path, boolean expand) {
		for (int i = 0; i < node.getChildCount(); i++) {
			TreeNode child = node.getChildAt(i);
			expandAllChildren(source, child, path.pathByAddingChild(child), expand);
		}
		if(source instanceof JTree){
			if (expand) {
				((JTree)source).expandPath(path);
			} else {
				((JTree)source).collapsePath(path);
			}
		} else {
			System.out.println("Source not JTree: " + source);
		}
	}

}
