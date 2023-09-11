package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

public class SetEventTrackAction implements UndoAction {
	private final EventObject eventObject;
	private final Sequence sequence;
	private final ArrayList<Integer> newTracks;
	private final TreeSet<Integer> oldTracks;
	private final ModelStructureChangeListener changeListener;
	private final String actionName;

	public SetEventTrackAction(EventObject eventObject, Sequence sequence, Collection<Integer> newTracks, ModelStructureChangeListener changeListener) {
		this.eventObject = eventObject;
		this.sequence = sequence;
		this.newTracks = new ArrayList<>(newTracks);
		this.oldTracks = eventObject.getEventTrack(sequence);
		this.changeListener = changeListener;
		actionName = oldTracks == null ? "added EventObject track" : "set EventObject track";
	}

	@Override
	public UndoAction undo() {
		eventObject.removeSequence(sequence);
		eventObject.addSequence(sequence, oldTracks);
		for(int track : newTracks){
			eventObject.removeTrack(sequence, track);
		}
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		eventObject.removeSequence(sequence);
		eventObject.addSequence(sequence, newTracks);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return actionName;
	}
}
