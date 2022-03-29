package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetAnimExtentAction implements UndoAction {
	private final ExtLog prevIntervalStart;
	private final ExtLog newIntervalStart;
	private final Animation animation;
	private final ModelStructureChangeListener changeListener;

	public SetAnimExtentAction(Animation animation, ExtLog newIntervalStart, ModelStructureChangeListener changeListener) {
		this.prevIntervalStart = animation.getExtents();
		this.newIntervalStart = newIntervalStart;
		this.animation = animation;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		animation.setExtents(prevIntervalStart);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animation.setExtents(newIntervalStart);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Edit extent of Animation " + animation.getName();
	}
}
