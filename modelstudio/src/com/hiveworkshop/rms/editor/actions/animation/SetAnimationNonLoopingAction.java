package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetAnimationNonLoopingAction implements UndoAction {
	private final boolean prevValue;
	private final boolean newValue;
	private final Animation animation;
	private final ModelStructureChangeListener changeListener;
	private final String actionName;

	public SetAnimationNonLoopingAction(boolean newValue, Animation animation, ModelStructureChangeListener changeListener) {
		this.animation = animation;
		this.prevValue = animation.isNonLooping();
		this.newValue = newValue;
		this.changeListener = changeListener;
		this.actionName = "Set " + animation.getName() + " to " + (newValue ? "NonLooping" : "Looping");
	}

	@Override
	public SetAnimationNonLoopingAction undo() {
		animation.setNonLooping(prevValue);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public SetAnimationNonLoopingAction redo() {
		animation.setNonLooping(newValue);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return actionName;
	}

}
