package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

public class EditEventTrackAction implements UndoAction {
	private final EventObject eventObject;
	private final Sequence sequence;
	private final int oldTrack;
	private final int newTrack;
	private final ModelStructureChangeListener changeListener;

	public EditEventTrackAction(EventObject eventObject, Sequence sequence, int oldTrack, int newTrack, ModelStructureChangeListener changeListener) {
		this.eventObject = eventObject;
		this.sequence = sequence;
		this.oldTrack = oldTrack;
		this.newTrack = newTrack;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		eventObject.removeTrack(sequence, newTrack);
		eventObject.addTrack(sequence, oldTrack);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		eventObject.removeTrack(sequence, oldTrack);
		eventObject.addTrack(sequence, newTrack);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "changed EventObject track time";
	}
}
