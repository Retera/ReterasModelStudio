package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

public class SetSequenceLengthAction implements UndoAction {
	private final int prevLength;
	private final int newLength;
	private final Sequence sequence;
	private final ModelStructureChangeListener changeListener;
	private final String actionName;

	public SetSequenceLengthAction(Sequence sequence, int newLength, ModelStructureChangeListener changeListener) {
		this.prevLength = sequence.getLength();
		this.newLength = newLength;
		this.sequence = sequence;
		if (sequence instanceof GlobalSeq) {
			actionName = "Set length of GlobalSeq (" + sequence.getLength() + ") to " + newLength;
		} else {
			actionName = "Set length of " + sequence.getName() + " to " + newLength;
		}
		this.changeListener = changeListener;
	}

	@Override
	public SetSequenceLengthAction undo() {
		sequence.setLength(prevLength);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public SetSequenceLengthAction redo() {
		sequence.setLength(newLength);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return actionName;
	}
}
