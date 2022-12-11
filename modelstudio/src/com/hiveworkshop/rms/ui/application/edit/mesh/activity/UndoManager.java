package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoHandler;

import java.util.ArrayDeque;
import java.util.Deque;

public class UndoManager {
	private final Deque<UndoAction> availableUndoActions;
	private final Deque<UndoAction> availableRedoActions;
	private final UndoHandler undoHandler;
	private int actionsSinceSave = 0;

	public UndoManager(final UndoHandler undoHandler) {
		this.undoHandler = undoHandler;
		this.availableUndoActions = new ArrayDeque<>();
		this.availableRedoActions = new ArrayDeque<>();
	}

	public void undo() {
		UndoAction action = availableUndoActions.pop();
		action.undo();
		actionsSinceSave--;
		availableRedoActions.push(action);
	}

	public void redo() {
		UndoAction action = availableRedoActions.pop();
		action.redo();
		actionsSinceSave++;
		availableUndoActions.push(action);
	}

	public void pushAction(UndoAction action) {
		availableUndoActions.push(action);
		availableRedoActions.clear();
		if (availableUndoActions.size() > ProgramGlobals.getPrefs().getMaxNumbersOfUndo()) {
			availableUndoActions.removeLast();
//			availableUndoActions.pollLast();
		}
		actionsSinceSave++;
		undoHandler.refreshUndo();
	}

	public boolean hasChangedSinceSave(){
		return actionsSinceSave == 0;
	}

	public void resetActionsSinceSave(){
		actionsSinceSave = 0;
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
