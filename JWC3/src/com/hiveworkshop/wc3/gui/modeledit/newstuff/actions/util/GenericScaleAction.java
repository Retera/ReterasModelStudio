package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;

public interface GenericScaleAction extends UndoAction {
	void updateScale(double scaleX, double scaleY, double scaleZ);
}
