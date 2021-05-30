package com.hiveworkshop.rms.editor.actions.util;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

public interface GenericMoveAction extends UndoAction {
	void updateTranslation(double deltaX, double deltaY, double deltaZ);

	GenericMoveAction updateTranslation(Vec3 delta);
}
