package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.animation;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Animation;

public class SetAnimationNonLoopingAction implements UndoAction {
	private final boolean prevValue;
	private final boolean newValue;
	private final Animation animation;
	private final ModelStructureChangeListener structureChangeListener;

	public SetAnimationNonLoopingAction(final boolean prevValue, final boolean newValue, final Animation animation,
			final ModelStructureChangeListener structureChangeListener) {
		this.prevValue = prevValue;
		this.newValue = newValue;
		this.animation = animation;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public void undo() {
		animation.setNonLooping(prevValue);
		structureChangeListener.animationParamsChanged(animation);
	}

	@Override
	public void redo() {
		animation.setNonLooping(newValue);
		structureChangeListener.animationParamsChanged(animation);
	}

	@Override
	public String actionName() {
		if (newValue) {
			return "set animation looping";
		}
		return "set animation non looping";
	}

}
