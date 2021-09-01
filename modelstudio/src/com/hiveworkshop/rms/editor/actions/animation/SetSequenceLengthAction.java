package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

public class SetSequenceLengthAction implements UndoAction {
	private final int prevIntervalLength;
	private final int newIntervalLength;
	private final Sequence sequence;
	private final String name;
	private final ModelStructureChangeListener changeListener;

	public SetSequenceLengthAction(Sequence sequence, int newIntervalLength, ModelStructureChangeListener changeListener) {
		this.prevIntervalLength = sequence.getLength();
		this.newIntervalLength = newIntervalLength;
		this.sequence = sequence;
		if (sequence instanceof GlobalSeq) {
			this.name = "GlobalSeq (" + sequence.getLength() + ")";
		} else {
			this.name = "Animation " + ((Animation) sequence).getName();
		}
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		sequence.setLength(prevIntervalLength);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		sequence.setLength(newIntervalLength);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set length of " + name + " to " + newIntervalLength;
	}
}
