package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener;

import java.util.List;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponent;

public interface ComponentVisibilityListener {
	UndoAction hideComponent(List<? extends SelectableComponent> selectableComponents,
			EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable);

	UndoAction showComponent(EditabilityToggleHandler editabilityToggleHandler);
}
