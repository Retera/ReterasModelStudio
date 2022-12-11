package com.hiveworkshop.rms.editor.actions.util;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

public interface GenericMoveAction extends UndoAction {
	GenericMoveAction updateTranslation(Vec3 delta);
}
