//
//package com.hiveworkshop.rms.ui.gui.modeledit.util;
//
//import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.CheckableDisplayElement;
//
//import javax.swing.tree.DefaultMutableTreeNode;
//
//public class JCheckBoxTreeNode extends DefaultMutableTreeNode {
//	private boolean checked;
//	private boolean tempHidden;
//	private ChildrenSelected childState = ChildrenSelected.NONE;
////	private CheckableDisplayElement<?> element;
//
//	public JCheckBoxTreeNode() {
//		super();
//	}
//
//	public JCheckBoxTreeNode(final CheckableDisplayElement<?> userObject, final boolean checked) {
//		super(userObject);
//		this.checked = checked;
////		element = userObject;
//	}
//
//	public JCheckBoxTreeNode(final CheckableDisplayElement<?> userObject) {
//		super(userObject);
//		this.checked = userObject.isVisible();
////		element = userObject;
//	}
//
//	public JCheckBoxTreeNode(final boolean checked) {
//		super();
//		this.checked = checked;
//	}
//
//	public JCheckBoxTreeNode setChecked(boolean checked) {
//		this.checked = checked;
//		tempHidden = false;
////		element.setVisible(checked);
//		return this;
//	}
//
//	public boolean isTempHidden() {
//		return tempHidden;
//	}
//
//	public boolean isChecked() {
////		return element.isVisible();
//		return checked;
//	}
//
//	public JCheckBoxTreeNode setTempHidden(boolean hidden) {
//		this.tempHidden = hidden;
//		return this;
//	}
//
//	public boolean isAllChildrenSelected() {
//		return childState == ChildrenSelected.ALL;
//	}
//
//	public JCheckBoxTreeNode setAllChildrenSelected(final boolean allChildrenSelected) {
//		childState = ChildrenSelected.ALL;
//		return this;
//	}
//
//	public ChildrenSelected getChildState() {
//		return childState;
//	}
//
//	public JCheckBoxTreeNode setChildState(ChildrenSelected childState) {
//		this.childState = childState;
//		return this;
//	}
//
//	public enum ChildrenSelected {
//		ALL, SOME, NONE
//	}
//}
