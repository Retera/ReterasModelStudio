package com.hiveworkshop.wc3.gui.modeledit.activity;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;

public interface UndoActionListener {
	void pushAction(UndoAction action);
}
