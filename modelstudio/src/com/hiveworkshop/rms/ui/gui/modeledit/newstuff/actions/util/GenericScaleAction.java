package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

public interface GenericScaleAction extends UndoAction {
	GenericScaleAction updateScale(Vec3 scale);
}
