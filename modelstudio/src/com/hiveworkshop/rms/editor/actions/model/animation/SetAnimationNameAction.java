package com.hiveworkshop.rms.editor.actions.model.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

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
	public UndoAction undo() {
		animation.setName(prevName);
		structureChangeListener.animationParamsChanged(animation);
		return this;
	}

	@Override
	public UndoAction redo() {
		animation.setName(newName);
		structureChangeListener.animationParamsChanged(animation);
		return this;
	}

	@Override
	public String actionName() {
		return "set animation name to \"" + newName + "\"";
	}
}
