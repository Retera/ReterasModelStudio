package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor;

import java.util.List;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;

public final class CompoundRotateAction implements GenericRotateAction {
	private final List<? extends GenericRotateAction> actions;
	private final String name;

	public CompoundRotateAction(final String name, final List<? extends GenericRotateAction> actions) {
		this.name = name;
		this.actions = actions;
	}

	@Override
	public void undo() {
		for (final UndoAction action : actions) {
			action.undo();
		}
	}

	@Override
	public void redo() {
		for (final UndoAction action : actions) {
			action.redo();
		}
	}

	@Override
	public String actionName() {
		return name;
	}

	@Override
	public void updateRotation(final double radians) {
		for (final GenericRotateAction action : actions) {
			action.updateRotation(radians);
		}
	}

}
