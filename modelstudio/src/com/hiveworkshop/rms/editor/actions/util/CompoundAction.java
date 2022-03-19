package com.hiveworkshop.rms.editor.actions.util;

import com.hiveworkshop.rms.editor.actions.UndoAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CompoundAction implements UndoAction {
	private final List<UndoAction> actions;
	private final String name;
	private Runnable updater;

	public CompoundAction(final String name, final List<UndoAction> actions) {
		this.name = name;
		this.actions = actions;
	}

	public CompoundAction(final String name, final List<UndoAction> actions, Runnable updater) {
		this.name = name;
		this.actions = actions;
		this.updater = updater;
	}

	public CompoundAction(final String name, final UndoAction action) {
		this.name = name;
		this.actions = Collections.singletonList(action);
	}

	public CompoundAction(final String name, Runnable updater, UndoAction... undoActions) {
		this.name = name;
		this.updater = updater;
		this.actions = new ArrayList<>(Arrays.asList(undoActions));
	}

	@Override
	public UndoAction undo() {
		for (int i = actions.size() - 1; i >= 0; i--) {
			final UndoAction action = actions.get(i);
			if (action != null) {
				action.undo();
			}
		}
		if (updater != null) {
			updater.run();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (final UndoAction action : actions) {
			if (action != null) {
				action.redo();
			}
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
