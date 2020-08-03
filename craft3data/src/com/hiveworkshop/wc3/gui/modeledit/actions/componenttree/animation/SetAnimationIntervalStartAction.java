package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.animation;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Animation;

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
