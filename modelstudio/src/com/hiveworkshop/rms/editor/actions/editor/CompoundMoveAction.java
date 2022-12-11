package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Arrays;
import java.util.List;

public final class CompoundMoveAction extends AbstractTransformAction{
	private final List<? extends AbstractTransformAction> actions;
	private final String name;

	public CompoundMoveAction(final String name, final List<? extends AbstractTransformAction> actions) {
		this.name = name;
		this.actions = actions;
	}

	public CompoundMoveAction(final String name, final AbstractTransformAction... actions) {
		this.name = name;
		this.actions = Arrays.asList(actions);
	}

	@Override
	public CompoundMoveAction undo() {
		for (final UndoAction action : actions) {
			action.undo();
		}
		return this;
	}

	@Override
	public CompoundMoveAction redo() {
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
	public CompoundMoveAction updateTranslation(Vec3 delta) {
		for (final AbstractTransformAction action : actions) {
			action.updateTranslation(delta);
		}
		return this;
	}

}
