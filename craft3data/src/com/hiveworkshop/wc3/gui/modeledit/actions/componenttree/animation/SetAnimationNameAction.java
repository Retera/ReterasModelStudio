package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.animation;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Animation;

public class SetAnimationNameAction implements UndoAction {
	private final String prevName;
	private final String newName;
	private final Animation animation;
	private final ModelStructureChangeListener structureChangeListener;

	public SetAnimationNameAction(final String prevName, final String newName, final Animation animation,
			final ModelStructureChangeListener structureChangeListener) {
		this.prevName = prevName;
		this.newName = newName;
		this.animation = animation;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public void undo() {
		animation.setName(prevName);
		structureChangeListener.animationParamsChanged(animation);
	}

	@Override
	public void redo() {
		animation.setName(newName);
		structureChangeListener.animationParamsChanged(animation);
	}

	@Override
	public String actionName() {
		return "set animation name to \"" + newName + "\"";
	}
}
