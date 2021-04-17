package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public interface GenericRotateAction extends UndoAction {
	void updateRotation(double radians);
}
