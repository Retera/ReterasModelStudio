package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

public interface GenericMoveAction extends UndoAction {
	void updateTranslation(double deltaX, double deltaY, double deltaZ);

	void updateTranslation(Vec3 delta);
}
