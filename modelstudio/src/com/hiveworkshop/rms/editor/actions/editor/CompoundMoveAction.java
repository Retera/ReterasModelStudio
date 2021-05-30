package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.util.Vec3;

import java.util.List;

public final class CompoundMoveAction implements GenericMoveAction {
	private final List<? extends GenericMoveAction> actions;
	private final String name;

	public CompoundMoveAction(final String name, final List<? extends GenericMoveAction> actions) {
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
	public void updateTranslation(final double deltaX, final double deltaY, final double deltaZ) {
		for (final GenericMoveAction action : actions) {
			action.updateTranslation(deltaX, deltaY, deltaZ);
		}
	}

	@Override
	public GenericMoveAction updateTranslation(Vec3 delta) {
		for (final GenericMoveAction action : actions) {
			action.updateTranslation(delta);
		}
		return this;
	}

}
