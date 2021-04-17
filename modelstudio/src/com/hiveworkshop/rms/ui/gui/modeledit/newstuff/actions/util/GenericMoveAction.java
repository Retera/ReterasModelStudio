package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public interface GenericMoveAction extends UndoAction {
	void updateTranslation(double deltaX, double deltaY, double deltaZ);
}
