package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

public class AddSequenceAction implements UndoAction {
	private final EditableModel model;
	private final Sequence sequence;
	private final String name;
	private final ModelStructureChangeListener changeListener;

	public AddSequenceAction(EditableModel model, Sequence sequence, ModelStructureChangeListener changeListener) {
		this.model = model;
		this.sequence = sequence;
		if (sequence instanceof GlobalSeq) {
			this.name = "GlobalSeq " + model.getGlobalSeqId((GlobalSeq) sequence) + " (" + sequence.getLength() + ")";
		} else {
			this.name = "Animation " + ((Animation) sequence).getName();
		}
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		if (sequence instanceof GlobalSeq) {
			model.remove((GlobalSeq) sequence);
		} else {
			model.remove((Animation) sequence);
		}
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		if (sequence instanceof GlobalSeq) {
			model.add((GlobalSeq) sequence);
		} else {
			model.add((Animation) sequence);
		}
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Added " + name;
	}
}
