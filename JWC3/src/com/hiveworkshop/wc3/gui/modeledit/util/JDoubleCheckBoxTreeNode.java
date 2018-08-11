
package com.hiveworkshop.wc3.gui.modeledit.util;

import javax.swing.tree.DefaultMutableTreeNode;

public class JDoubleCheckBoxTreeNode extends DefaultMutableTreeNode {
	private boolean checked;
	private boolean allChildrenSelected;
	private boolean hasPersonalState; // NYI, do not use

	public JDoubleCheckBoxTreeNode() {
		super();
	}

	public JDoubleCheckBoxTreeNode(final Object userObject, final boolean checked) {
		super(userObject);
		this.checked = checked;
	}

	public JDoubleCheckBoxTreeNode(final Object userObject) {
		super(userObject);
	}

	public JDoubleCheckBoxTreeNode(final boolean checked) {
		super();
		this.checked = checked;
	}

	public void setChecked(final boolean checked) {
		this.checked = checked;
	}

	public boolean isChecked() {
		return checked;
	}

	public boolean isAllChildrenSelected() {
		return allChildrenSelected;
	}

	public void setAllChildrenSelected(final boolean allChildrenSelected) {
		this.allChildrenSelected = allChildrenSelected;
	}

	// NYI, buggy, do not use
	public void setHasPersonalState(final boolean hasPersonalState) {
		this.hasPersonalState = hasPersonalState;
	}

	// NYI, buggy, do not use
	public boolean isHasPersonalState() {
		return hasPersonalState;
	}
}
