package com.hiveworkshop.rms.ui.application.actions.model.animation;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.editor.model.Animation;

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
	public void undo() {
		animation.setIntervalStart(prevIntervalStart);
		structureChangeListener.animationParamsChanged(animation);
	}

	@Override
	public void redo() {
		animation.setIntervalStart(newIntervalStart);
		structureChangeListener.animationParamsChanged(animation);
	}

	@Override
	public String actionName() {
		return "set animation IntervalStart to " + newIntervalStart;
	}
}
