package com.hiveworkshop.rms.editor.actions.model.globalsequence;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetGlobalSequenceLengthAction implements UndoAction {
	private final EditableModel model;
	private final int globalSequenceId;
	private final Integer previousLength;
	private final Integer newLength;
	private final ModelStructureChangeListener structureChangeListener;

	public SetGlobalSequenceLengthAction(final EditableModel model, final int globalSequenceId, final Integer previousLength,
			final Integer newLength, final ModelStructureChangeListener structureChangeListener) {
		this.model = model;
		this.globalSequenceId = globalSequenceId;
		this.previousLength = previousLength;
		this.newLength = newLength;
		this.structureChangeListener = structureChangeListener;
	}

	@Override
	public UndoAction undo() {
		model.setGlobalSequenceLength(globalSequenceId, previousLength);
		structureChangeListener.globalSequenceLengthChanged(globalSequenceId, previousLength);
		return this;
	}

	@Override
	public UndoAction redo() {
		model.setGlobalSequenceLength(globalSequenceId, newLength);
		structureChangeListener.globalSequenceLengthChanged(globalSequenceId, newLength);
		return this;
	}

	@Override
	public String actionName() {
		return "change GlobalSequence length to " + newLength;
	}
}
