package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
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
	public void updateTranslation(final double deltaX, final double deltaY, final double deltaZ) {
		for (final GenericMoveAction action : actions) {
			action.updateTranslation(deltaX, deltaY, deltaZ);
		}
	}

	@Override
	public void updateTranslation(Vec3 delta) {
		for (final GenericMoveAction action : actions) {
			action.updateTranslation(delta);
		}
	}

}
