package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;

public interface GenericMoveAction extends UndoAction {
	void updateTranslation(double deltaX, double deltaY, double deltaZ);
}
