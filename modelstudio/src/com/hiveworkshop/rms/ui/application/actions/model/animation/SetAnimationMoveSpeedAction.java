package com.hiveworkshop.rms.ui.application.actions.model.animation;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.editor.model.Animation;

public class SetAnimationMoveSpeedAction implements UndoAction {
	private final float prevMoveSpeed;
	private final float newMoveSpeed;
	private final Animation animation;
	private final ModelStructureChangeListener structureChangeListener;

	public SetAnimationMoveSpeedAction(final float prevMoveSpeed, final float newMoveSpeed, final Animation animation,
			final ModelStructureChangeListener structureChangeListener) {
		this.prevMoveSpeed = prevMoveSpeed;
		this.newMoveSpeed = newMoveSpeed;
		this.animation = animation;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public void undo() {
		animation.setMoveSpeed(prevMoveSpeed);
		structureChangeListener.animationParamsChanged(animation);
	}

	@Override
	public void redo() {
		animation.setMoveSpeed(newMoveSpeed);
		structureChangeListener.animationParamsChanged(animation);
	}

	@Override
	public String actionName() {
		return "set animation MoveSpeed to " + newMoveSpeed;
	}
}
