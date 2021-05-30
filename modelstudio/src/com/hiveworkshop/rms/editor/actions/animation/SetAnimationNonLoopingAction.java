package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetAnimationNonLoopingAction implements UndoAction {
	private final boolean prevValue;
	private final boolean newValue;
	private final Animation animation;
	private final ModelStructureChangeListener structureChangeListener;

	public SetAnimationNonLoopingAction(final boolean prevValue, final boolean newValue, final Animation animation,
			final ModelStructureChangeListener structureChangeListener) {
		this.prevValue = prevValue;
		this.newValue = newValue;
		this.animation = animation;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public UndoAction undo() {
		animation.setNonLooping(prevValue);
		structureChangeListener.animationParamsChanged(animation);
		return this;
	}

	@Override
	public UndoAction redo() {
		animation.setNonLooping(newValue);
		structureChangeListener.animationParamsChanged(animation);
		return this;
	}

	@Override
	public String actionName() {
		if (newValue) {
			return "set animation looping";
		}
		return "set animation non looping";
	}

}
