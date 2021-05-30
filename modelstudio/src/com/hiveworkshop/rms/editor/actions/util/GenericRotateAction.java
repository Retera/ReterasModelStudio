package com.hiveworkshop.rms.editor.actions.util;

import com.hiveworkshop.rms.editor.actions.UndoAction;

public interface GenericRotateAction extends UndoAction {
	GenericRotateAction updateRotation(double radians);
}
