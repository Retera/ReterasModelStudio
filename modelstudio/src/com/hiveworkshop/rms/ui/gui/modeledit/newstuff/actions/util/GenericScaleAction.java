package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public interface GenericScaleAction extends UndoAction {
	void updateScale(double scaleX, double scaleY, double scaleZ);
}
