package com.hiveworkshop.wc3.gui.modeledit.newstuff.listener;

import com.etheller.collections.ListView;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectableComponent;

public interface ComponentVisibilityListener {
	UndoAction hideComponent(ListView<? extends SelectableComponent> selectableComponents,
			EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable);

	UndoAction showComponent(EditabilityToggleHandler editabilityToggleHandler);
}
