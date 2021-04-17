
package com.hiveworkshop.rms.ui.gui.modeledit.util;

import javax.swing.tree.DefaultMutableTreeNode;

public class JDoubleCheckBoxTreeNode extends DefaultMutableTreeNode {
	private boolean checked;
	private boolean checked2;
	private boolean allChildrenSelected;
	private boolean allChildrenV2Selected;
	private boolean hasPersonalState; // NYI, do not use
	private boolean hasPersonalState2;

	public JDoubleCheckBoxTreeNode() {
		super();
	}

	public JDoubleCheckBoxTreeNode(final Object userObject, final boolean checked, boolean checked2) {
		super(userObject);
		this.checked = checked;
		this.checked2 = checked2;
	}

	public JDoubleCheckBoxTreeNode(final Object userObject) {
		super(userObject);
	}

	public JDoubleCheckBoxTreeNode(final boolean checked, boolean checked2) {
		super();
		this.checked = checked;
		this.checked2 = checked2;
	}

	public void setChecked(final boolean checked) {
		this.checked = checked;
	}
	
	public void setChecked2(boolean checked2) {
		this.checked2 = checked2;
	}

	public boolean isChecked() {
		return checked;
	}
	
	public boolean isChecked2() {
		return checked2;
	}

	public boolean isAllChildrenSelected() {
		return allChildrenSelected;
	}
	
	public boolean isAllChildrenV2Selected() {
		return allChildrenV2Selected;
	}

	public void setAllChildrenSelected(final boolean allChildrenSelected) {
		this.allChildrenSelected = allChildrenSelected;
	}
	
	public void setAllChildrenV2Selected(boolean allChildrenV2Selected) {
		this.allChildrenV2Selected = allChildrenV2Selected;
	}

	// NYI, buggy, do not use
	public void setHasPersonalState(final boolean hasPersonalState) {
		this.hasPersonalState = hasPersonalState;
	}

	// NYI, buggy, do not use
	public boolean isHasPersonalState() {
		return hasPersonalState;
	}
	
	public void setHasPersonalState2(boolean hasPersonalState2) {
		this.hasPersonalState2 = hasPersonalState2;
	}
	
	public boolean isHasPersonalState2() {
		return hasPersonalState2;
	}
}
