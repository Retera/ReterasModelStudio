package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public interface UndoActionListener {
	void pushAction(UndoAction action);
}
