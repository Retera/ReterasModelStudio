package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetAnimationIntervalStartAction implements UndoAction {
	private final int prevIntervalStart;
	private final int newIntervalStart;
	private final Animation animation;
	private final ModelStructureChangeListener structureChangeListener;

	public SetAnimationIntervalStartAction(final int prevIntervalStart, final int newIntervalStart,
			final Animation animation, final ModelStructureChangeListener structureChangeListener) {
		this.prevIntervalStart = prevIntervalStart;
		this.newIntervalStart = newIntervalStart;
		this.animation = animation;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public UndoAction undo() {
		animation.setIntervalStart(prevIntervalStart);
		structureChangeListener.animationParamsChanged(animation);
		return this;
	}

	@Override
	public UndoAction redo() {
		animation.setIntervalStart(newIntervalStart);
		structureChangeListener.animationParamsChanged(animation);
		return this;
	}

	@Override
	public String actionName() {
		return "set animation IntervalStart to " + newIntervalStart;
	}
}
