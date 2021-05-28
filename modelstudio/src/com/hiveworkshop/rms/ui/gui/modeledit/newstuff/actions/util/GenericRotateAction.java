package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public interface GenericRotateAction extends UndoAction {
	GenericRotateAction updateRotation(double radians);
}
