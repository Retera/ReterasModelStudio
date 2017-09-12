
package com.hiveworkshop.wc3.gui.modeledit.util;

import javax.swing.tree.DefaultMutableTreeNode;

public class JCheckBoxTreeNode extends DefaultMutableTreeNode {
	private boolean checked;
	private boolean allChildrenSelected;

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
}
