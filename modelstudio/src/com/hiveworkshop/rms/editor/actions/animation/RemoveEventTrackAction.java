package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

public class RemoveEventTrackAction implements UndoAction {
	private final EventObject eventObject;
	private final Sequence sequence;
	private final int oldTrack;
	private final ModelStructureChangeListener changeListener;

	public RemoveEventTrackAction(EventObject eventObject, Sequence sequence, int oldTrack, ModelStructureChangeListener changeListener) {
		this.eventObject = eventObject;
		this.sequence = sequence;
		this.oldTrack = oldTrack;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		eventObject.addTrack(sequence, oldTrack);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		eventObject.removeTrack(sequence, oldTrack);
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
