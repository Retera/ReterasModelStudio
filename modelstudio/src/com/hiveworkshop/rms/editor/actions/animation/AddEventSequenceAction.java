package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

public class AddEventSequenceAction implements UndoAction {
	private final EventObject eventObject;
	private final Sequence sequence;
	private final ModelStructureChangeListener changeListener;

	public AddEventSequenceAction(EventObject eventObject, Sequence sequence, ModelStructureChangeListener changeListener) {
		this.eventObject = eventObject;
		this.sequence = sequence;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		eventObject.removeSequence(sequence);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		eventObject.addTrack(sequence, 0);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "removed EventObject track";
	}
}
