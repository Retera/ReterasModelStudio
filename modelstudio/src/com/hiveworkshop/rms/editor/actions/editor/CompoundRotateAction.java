package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericRotateAction;

import java.util.List;

public final class CompoundRotateAction implements GenericRotateAction {
	private final List<? extends GenericRotateAction> actions;
	private final String name;

	public CompoundRotateAction(final String name, final List<? extends GenericRotateAction> actions) {
		this.name = name;
		this.actions = actions;
	}

	@Override
	public UndoAction undo() {
		for (final UndoAction action : actions) {
			action.undo();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (final UndoAction action : actions) {
			action.redo();
		}
		return this;
	}

	@Override
	public String actionName() {
		return name;
	}

	@Override
	public GenericRotateAction updateRotation(final double radians) {
		for (final GenericRotateAction action : actions) {
			action.updateRotation(radians);
		}
		return this;
	}

}
