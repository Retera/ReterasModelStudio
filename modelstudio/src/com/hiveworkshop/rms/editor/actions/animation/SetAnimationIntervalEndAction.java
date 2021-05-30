package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetAnimationIntervalEndAction implements UndoAction {
	private final int prevIntervalEnd;
	private final int newIntervalEnd;
	private final Animation animation;
	private final ModelStructureChangeListener structureChangeListener;

	public SetAnimationIntervalEndAction(final int prevIntervalEnd, final int newIntervalEnd, final Animation animation,
			final ModelStructureChangeListener structureChangeListener) {
		this.prevIntervalEnd = prevIntervalEnd;
		this.newIntervalEnd = newIntervalEnd;
		this.animation = animation;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public UndoAction undo() {
		animation.setIntervalEnd(prevIntervalEnd);
		structureChangeListener.animationParamsChanged(animation);
		return this;
	}

	@Override
	public UndoAction redo() {
		animation.setIntervalEnd(newIntervalEnd);
		structureChangeListener.animationParamsChanged(animation);
		return this;
	}

	@Override
	public String actionName() {
		return "set animation IntervalEnd to " + newIntervalEnd;
	}
}
