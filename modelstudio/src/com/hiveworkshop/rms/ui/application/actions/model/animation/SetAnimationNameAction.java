package com.hiveworkshop.rms.ui.application.actions.model.animation;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.editor.model.Animation;

public class SetAnimationNameAction implements UndoAction {
	private final String prevName;
	private final String newName;
	private final Animation animation;
	private final ModelStructureChangeListener structureChangeListener;

	public SetAnimationNameAction(final String prevName, final String newName, final Animation animation,
			final ModelStructureChangeListener structureChangeListener) {
		this.prevName = prevName;
		this.newName = newName;
		this.animation = animation;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public void undo() {
		animation.setName(prevName);
		structureChangeListener.animationParamsChanged(animation);
	}

	@Override
	public void redo() {
		animation.setName(newName);
		structureChangeListener.animationParamsChanged(animation);
	}

	@Override
	public String actionName() {
		return "set animation name to \"" + newName + "\"";
	}
}
