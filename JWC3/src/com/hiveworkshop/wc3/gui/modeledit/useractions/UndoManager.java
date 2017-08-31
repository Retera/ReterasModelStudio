package com.hiveworkshop.wc3.gui.modeledit.useractions;

public interface UndoManager extends UndoActionListener {
	void undo();

	void redo();
}
