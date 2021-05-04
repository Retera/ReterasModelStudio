package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoHandler;

import java.util.ArrayDeque;
import java.util.Deque;

public class UndoManager {
	private final Deque<UndoAction> availableUndoActions;
	private final Deque<UndoAction> availableRedoActions;
	private final UndoHandler undoHandler;

	public UndoManager(final UndoHandler undoHandler) {
		this.undoHandler = undoHandler;
		this.availableUndoActions = new ArrayDeque<>();
		this.availableRedoActions = new ArrayDeque<>();
	}

	public void undo() {
		UndoAction action = availableUndoActions.pop();
		action.undo();
		availableRedoActions.push(action);
	}

	public void redo() {
		UndoAction action = availableRedoActions.pop();
		action.redo();
		availableUndoActions.push(action);
	}

	public void pushAction(UndoAction action) {
		availableUndoActions.push(action);
		availableRedoActions.clear();
		undoHandler.refreshUndo();
	}

	public boolean isUndoListEmpty() {
		return availableUndoActions.isEmpty();
	}

	public String getUndoText() {
		if (availableUndoActions.isEmpty()) {
			return "";
		}
		return availableUndoActions.peek().actionName();
	}

	public String getRedoText() {
		if (availableRedoActions.isEmpty()) {
			return "";
		}
		return availableRedoActions.peek().actionName();
	}

	public boolean isRedoListEmpty() {
		return availableRedoActions.isEmpty();
	}
}
