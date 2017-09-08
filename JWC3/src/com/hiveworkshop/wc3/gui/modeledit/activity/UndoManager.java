package com.hiveworkshop.wc3.gui.modeledit.activity;

public interface UndoManager extends UndoActionListener {
	void undo();

	void redo();
}
