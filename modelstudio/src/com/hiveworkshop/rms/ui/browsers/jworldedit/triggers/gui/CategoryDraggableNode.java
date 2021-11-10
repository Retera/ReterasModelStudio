package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui;

import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.TriggerCategory;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.Serializable;

final class CategoryDraggableNode implements DraggableNode, Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -2892510828683155370L;
	private final TriggerCategory category;

	public CategoryDraggableNode(final TriggerCategory category) {
		this.category = category;
	}

	public TriggerCategory getCategory() {
		return category;
	}

	@Override
	public void dragInto(final TriggerTreeController controller, final DefaultMutableTreeNode rootGuy,
	                     final int index) {
		controller.moveCategory(category, index);
	}

}
