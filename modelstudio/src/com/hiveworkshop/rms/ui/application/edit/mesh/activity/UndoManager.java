package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

public interface UndoManager extends UndoActionListener {
	void undo();

	void redo();

	boolean isUndoListEmpty();

	boolean isRedoListEmpty();

	String getUndoText();

	String getRedoText();
}
