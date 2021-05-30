package com.hiveworkshop.rms.editor.actions.util;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

public interface GenericScaleAction extends UndoAction {
	GenericScaleAction updateScale(Vec3 scale);
}
