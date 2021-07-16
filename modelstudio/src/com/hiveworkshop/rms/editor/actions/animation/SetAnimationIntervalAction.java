package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetAnimationIntervalAction implements UndoAction {
	private final int prevIntervalStart;
	private final int newIntervalStart;
	private final int prevIntervalEnd;
	private final int newIntervalEnd;
	private final Animation animation;
	private final ModelStructureChangeListener changeListener;

	public SetAnimationIntervalAction(int newIntervalStart, int newIntervalEnd, Animation animation, ModelStructureChangeListener changeListener) {
		this.prevIntervalStart = animation.getStart();
		this.newIntervalStart = newIntervalStart;
		this.prevIntervalEnd = animation.getEnd();
		this.newIntervalEnd = newIntervalEnd;
		this.animation = animation;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		animation.setIntervalStart(prevIntervalStart);
		animation.setIntervalEnd(prevIntervalEnd);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animation.setIntervalStart(newIntervalStart);
		animation.setIntervalEnd(newIntervalEnd);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set animation IntervalEnd to " + newIntervalEnd;
	}
}
