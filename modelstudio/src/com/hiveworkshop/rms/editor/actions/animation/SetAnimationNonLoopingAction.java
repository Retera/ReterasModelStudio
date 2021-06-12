package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetAnimationNonLoopingAction implements UndoAction {
	private final boolean prevValue;
	private final boolean newValue;
	private final Animation animation;
	private final ModelStructureChangeListener changeListener;

	public SetAnimationNonLoopingAction(boolean newValue, Animation animation, ModelStructureChangeListener changeListener) {
		this.animation = animation;
		this.prevValue = animation.isNonLooping();
		this.newValue = newValue;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		animation.setNonLooping(prevValue);
		changeListener.animationParamsChanged(animation);
		return this;
	}

	@Override
	public UndoAction redo() {
		animation.setNonLooping(newValue);
		changeListener.animationParamsChanged(animation);
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
