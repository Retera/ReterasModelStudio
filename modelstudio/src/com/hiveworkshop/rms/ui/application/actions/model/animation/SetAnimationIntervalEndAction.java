package com.hiveworkshop.rms.ui.application.actions.model.animation;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.editor.model.Animation;

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
	public void undo() {
		animation.setIntervalEnd(prevIntervalEnd);
		structureChangeListener.animationParamsChanged(animation);
	}

	@Override
	public void redo() {
		animation.setIntervalEnd(newIntervalEnd);
		structureChangeListener.animationParamsChanged(animation);
	}

	@Override
	public String actionName() {
		return "set animation IntervalEnd to " + newIntervalEnd;
	}
}
