package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.TreeSet;

public class RemoveEventSequenceAction implements UndoAction {
	private final EventObject eventObject;
	private final Sequence sequence;
	private final TreeSet<Integer> oldTracks;
	private final ModelStructureChangeListener changeListener;

	public RemoveEventSequenceAction(EventObject eventObject, Sequence sequence, ModelStructureChangeListener changeListener) {
		this.eventObject = eventObject;
		this.sequence = sequence;
		oldTracks = eventObject.getEventTrack(sequence);
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		eventObject.setSequence(sequence, oldTracks);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		eventObject.removeSequence(sequence);
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
