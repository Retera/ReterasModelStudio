package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui;

import javax.swing.tree.DefaultMutableTreeNode;

public interface DraggableNode {
	void dragInto(TriggerTreeController controller, DefaultMutableTreeNode rootGuy, int index);
}
