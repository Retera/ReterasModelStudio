package com.hiveworkshop.rms.ui.gui.modeledit;

/**
 * Write a description of class UndoAction here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public interface UndoAction {
	UndoAction undo();

	UndoAction redo();

	String actionName();
}
