package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetAnimationMoveSpeedAction implements UndoAction {
	private final float prevMoveSpeed;
	private final float newMoveSpeed;
	private final Animation animation;
	private final ModelStructureChangeListener changeListener;
	private final String actionName;

	public SetAnimationMoveSpeedAction(float newMoveSpeed, Animation animation, ModelStructureChangeListener changeListener) {
		this.animation = animation;
		this.prevMoveSpeed = animation.getMoveSpeed();
		this.newMoveSpeed = newMoveSpeed;
		this.changeListener = changeListener;
		this.actionName = "Set MoveSpeed of " + animation.getName() + " to " + newMoveSpeed;
	}

	@Override
	public SetAnimationMoveSpeedAction undo() {
		animation.setMoveSpeed(prevMoveSpeed);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public SetAnimationMoveSpeedAction redo() {
		animation.setMoveSpeed(newMoveSpeed);
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
