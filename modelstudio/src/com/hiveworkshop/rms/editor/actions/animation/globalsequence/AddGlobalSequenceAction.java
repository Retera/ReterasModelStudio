package com.hiveworkshop.rms.editor.actions.animation.globalsequence;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class AddGlobalSequenceAction implements UndoAction {
	private final EditableModel model;
	private final GlobalSeq globalSeq;
	private final ModelStructureChangeListener changeListener;

	public AddGlobalSequenceAction(EditableModel model, GlobalSeq globalSeq, ModelStructureChangeListener changeListener) {
		this.model = model;
		this.globalSeq = globalSeq;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		model.remove(globalSeq);
		if (changeListener != null) {
			changeListener.globalSequenceLengthChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		model.add(globalSeq);
		if (changeListener != null) {
			changeListener.globalSequenceLengthChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "added Global sequence with length " + globalSeq.getLength();
	}
}
