package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class AddEventTrackAction implements UndoAction {
	private final EventObject eventObject;
	private final Sequence sequence;
	private final ArrayList<Integer> newTracks;
	private final ModelStructureChangeListener changeListener;

	public AddEventTrackAction(EventObject eventObject, Sequence sequence, int newTrack, ModelStructureChangeListener changeListener) {
		this(eventObject, sequence, Collections.singleton(newTrack), changeListener);
	}
	public AddEventTrackAction(EventObject eventObject, Sequence sequence, Collection<Integer> newTracks, ModelStructureChangeListener changeListener) {
		this.eventObject = eventObject;
		this.sequence = sequence;
		this.newTracks = new ArrayList<>(newTracks);
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
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
		for(int track : newTracks){
			eventObject.addTrack(sequence, track);
		}
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "added EventObject track";
	}
}
