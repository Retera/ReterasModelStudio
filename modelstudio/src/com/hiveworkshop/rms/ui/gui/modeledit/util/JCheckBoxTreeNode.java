
package com.hiveworkshop.rms.ui.gui.modeledit.util;

import javax.swing.tree.DefaultMutableTreeNode;

public class JCheckBoxTreeNode extends DefaultMutableTreeNode {
	private boolean checked;
	private boolean tempHidden;
	private ChildrenSelected childState = ChildrenSelected.NONE;

	public JCheckBoxTreeNode() {
		super();
	}

	public JCheckBoxTreeNode(final Object userObject, final boolean checked) {
		super(userObject);
		this.checked = checked;
	}

	public JCheckBoxTreeNode(final Object userObject) {
		super(userObject);
	}

	public JCheckBoxTreeNode(final boolean checked) {
		super();
		this.checked = checked;
	}

	public JCheckBoxTreeNode setChecked(boolean checked) {
		this.checked = checked;
		tempHidden = false;
		return this;
	}

	public boolean isTempHidden() {
		return tempHidden;
	}

	public boolean isChecked() {
		return checked;
	}

	public JCheckBoxTreeNode setTempHidden(boolean hidden) {
		this.tempHidden = hidden;
		return this;
	}

	public boolean isAllChildrenSelected() {
		return childState == ChildrenSelected.ALL;
	}

	public JCheckBoxTreeNode setAllChildrenSelected(final boolean allChildrenSelected) {
		childState = ChildrenSelected.ALL;
		return this;
	}

	public ChildrenSelected getChildState() {
		return childState;
	}

	public JCheckBoxTreeNode setChildState(ChildrenSelected childState) {
		this.childState = childState;
		return this;
	}

	public enum ChildrenSelected {
		ALL, SOME, NONE
	}
}
