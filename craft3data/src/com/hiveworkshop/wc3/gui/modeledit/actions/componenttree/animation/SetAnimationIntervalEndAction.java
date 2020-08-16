package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.animation;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Animation;

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
