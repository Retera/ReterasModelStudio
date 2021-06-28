package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetAnimationIntervalStartAction implements UndoAction {
	private final int prevIntervalStart;
	private final int newIntervalStart;
	private final Animation animation;
	private final ModelStructureChangeListener changeListener;

	public SetAnimationIntervalStartAction(int newIntervalStart,
	                                       Animation animation,
	                                       ModelStructureChangeListener changeListener) {
		this.prevIntervalStart = animation.getStart();
		this.newIntervalStart = newIntervalStart;
		this.animation = animation;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		animation.setIntervalStart(prevIntervalStart);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animation.setIntervalStart(newIntervalStart);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set animation IntervalStart to " + newIntervalStart;
	}
}
