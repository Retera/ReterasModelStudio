package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetAnimationNameAction implements UndoAction {
	private final String prevName;
	private final String newName;
	private final Animation animation;
	private final ModelStructureChangeListener changeListener;

	public SetAnimationNameAction(String newName, Animation animation,
	                              ModelStructureChangeListener changeListener) {
		this.prevName = animation.getName();
		this.newName = newName;
		this.animation = animation;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		animation.setName(prevName);
		if (changeListener != null) {
			changeListener.animationParamsChanged(animation);
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animation.setName(newName);
		if (changeListener != null) {
			changeListener.animationParamsChanged(animation);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set animation name to \"" + newName + "\"";
	}
}
