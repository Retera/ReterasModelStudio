package com.hiveworkshop.rms.editor.actions.animation.globalsequence;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetGlobalSequenceLengthAction implements UndoAction {
	private final EditableModel model;
	private final int globalSequenceId;
	private final Integer previousLength;
	private final Integer newLength;
	private final ModelStructureChangeListener changeListener;

	public SetGlobalSequenceLengthAction(final EditableModel model, final int globalSequenceId, final Integer previousLength,
	                                     final Integer newLength, final ModelStructureChangeListener changeListener) {
		this.model = model;
		this.globalSequenceId = globalSequenceId;
		this.previousLength = previousLength;
		this.newLength = newLength;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		model.setGlobalSequenceLength(globalSequenceId, previousLength);
		if (changeListener != null) {
			changeListener.globalSequenceLengthChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		model.setGlobalSequenceLength(globalSequenceId, newLength);
		if (changeListener != null) {
			changeListener.globalSequenceLengthChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "change GlobalSequence length to " + newLength;
	}
}
