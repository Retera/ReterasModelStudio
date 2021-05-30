package com.hiveworkshop.rms.editor.actions.util;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

public class DoNothingMoveActionAdapter implements GenericMoveAction {

	private final UndoAction delegate;

	public DoNothingMoveActionAdapter(final UndoAction delegate) {
		this.delegate = delegate;
	}

	@Override
	public UndoAction undo() {
		delegate.undo();
		return this;
	}

	@Override
	public UndoAction redo() {
		delegate.redo();
		return this;
	}

	@Override
	public String actionName() {
		return delegate.actionName();
	}

	@Override
	public void updateTranslation(final double deltaX, final double deltaY, final double deltaZ) {
	}

	@Override
	public GenericMoveAction updateTranslation(Vec3 delta) {
		return this;
	}

}
