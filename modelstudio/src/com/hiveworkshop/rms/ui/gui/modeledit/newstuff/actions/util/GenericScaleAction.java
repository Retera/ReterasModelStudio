package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

public interface GenericScaleAction extends UndoAction {
	void updateScale(double scaleX, double scaleY, double scaleZ);

	void updateScale(Vec3 scale);
}
