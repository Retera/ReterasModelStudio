package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.util.Vec3;

import java.util.List;

public final class CompoundScaleAction implements GenericScaleAction {
	private final List<? extends GenericScaleAction> actions;
	private final String name;

	public CompoundScaleAction(final String name, final List<? extends GenericScaleAction> actions) {
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
	public void updateScale(final double scaleX, final double scaleY, final double scaleZ) {
		for (final GenericScaleAction action : actions) {
			action.updateScale(scaleX, scaleY, scaleZ);
		}
	}

	@Override
	public void updateScale(final Vec3 scale) {
		for (final GenericScaleAction action : actions) {
			action.updateScale(scale);
		}
	}

}
