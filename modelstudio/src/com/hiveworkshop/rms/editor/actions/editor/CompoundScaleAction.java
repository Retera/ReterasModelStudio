package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

import java.util.List;

public final class CompoundScaleAction extends AbstractTransformAction {
	private final List<? extends AbstractTransformAction> actions;
	private final String name;

	public CompoundScaleAction(final String name, final List<? extends AbstractTransformAction> actions) {
		this.name = name;
		this.actions = actions;
	}

	@Override
	public CompoundScaleAction undo() {
		for (final UndoAction action : actions) {
			action.undo();
		}
		return this;
	}

	@Override
	public CompoundScaleAction redo() {
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
	public CompoundScaleAction updateScale(final Vec3 scale) {
		for (final AbstractTransformAction action : actions) {
			action.updateScale(scale);
		}
		return this;
	}

}
