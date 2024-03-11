package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui;

import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.Trigger;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.Serializable;

final class TriggerDraggableNode implements DraggableNode, Serializable {
	private static final long serialVersionUID = -3149168228812835946L;
	private final Trigger trigger;

	public TriggerDraggableNode(final Trigger trigger) {
		this.trigger = trigger;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	@Override
	public void dragInto(final TriggerTreeController controller, final DefaultMutableTreeNode rootGuy, final int index) {
		controller.moveTrigger(trigger, ((TriggerCategoryTreeNode) rootGuy).getCategory(), index);
	}

}
