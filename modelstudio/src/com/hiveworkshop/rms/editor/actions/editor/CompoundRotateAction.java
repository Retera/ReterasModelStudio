package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.UndoAction;

import java.util.List;

public final class CompoundRotateAction extends AbstractTransformAction {
	private final List<? extends AbstractTransformAction> actions;
	private final String name;

	public CompoundRotateAction(final String name, final List<? extends AbstractTransformAction> actions) {
		this.name = name;
		this.actions = actions;
	}

	@Override
	public CompoundRotateAction undo() {
		for (final UndoAction action : actions) {
			action.undo();
		}
		return this;
	}

	@Override
	public CompoundRotateAction redo() {
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
	public CompoundRotateAction updateRotation(final double radians) {
		for (final AbstractTransformAction action : actions) {
			action.updateRotation(radians);
		}
		return this;
	}

	@Override
	public CompoundRotateAction setRotation(final double radians) {
		for (final AbstractTransformAction action : actions) {
			action.setRotation(radians);
		}
		return this;
	}

}
