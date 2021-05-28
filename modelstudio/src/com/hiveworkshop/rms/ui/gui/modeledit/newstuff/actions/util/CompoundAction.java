package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.Collections;
import java.util.List;

public final class CompoundAction implements UndoAction {
	private final List<? extends UndoAction> actions;
	private final String name;
	private Runnable updater;

	public CompoundAction(final String name, final List<? extends UndoAction> actions) {
		this.name = name;
		this.actions = actions;
	}

	public CompoundAction(final String name, final List<? extends UndoAction> actions, Runnable updater) {
		this.name = name;
		this.actions = actions;
		this.updater = updater;
	}

	public CompoundAction(final String name, final UndoAction action) {
		this.name = name;
		this.actions = Collections.singletonList(action);
	}

	@Override
	public UndoAction undo() {
		for (int i = actions.size() - 1; i >= 0; i--) {
			final UndoAction action = actions.get(i);
			action.undo();
		}
		if (updater != null) {
			updater.run();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (final UndoAction action : actions) {
			action.redo();
		}
		if (updater != null) {
			updater.run();
		}
		return this;
	}

	@Override
	public String actionName() {
		return name;
	}

}
