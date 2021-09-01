package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetAnimationStartAction implements UndoAction {
	private final int prevIntervalStart;
	private final int newIntervalStart;
	private final Animation animation;
	private final ModelStructureChangeListener changeListener;

	public SetAnimationStartAction(Animation animation, int newIntervalStart,
	                               ModelStructureChangeListener changeListener) {
		this.prevIntervalStart = animation.getStart();
		this.newIntervalStart = newIntervalStart;
		this.animation = animation;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		animation.setStart(prevIntervalStart);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animation.setStart(newIntervalStart);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set start of Animation " + animation.getName() + " to " + newIntervalStart;
	}
}
