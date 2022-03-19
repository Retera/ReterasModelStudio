package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveFlagEntryMapAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.*;

public class RemoveSequenceAction implements UndoAction {
	private final EditableModel model;
	private final Sequence sequence;
	private final String name;
	private final ModelStructureChangeListener changeListener;
	private final Map<EventObject, Set<Integer>> eventTracksToRemove = new HashMap<>();
	private final List<RemoveFlagEntryMapAction<?>> removeFlagEntryMapActions = new ArrayList<>();

	public RemoveSequenceAction(EditableModel model, Sequence sequence, ModelStructureChangeListener changeListener) {
		this.model = model;
		this.sequence = sequence;
		this.changeListener = changeListener;
		if (sequence instanceof GlobalSeq) {
			this.name = "GlobalSeq " + model.getGlobalSeqId((GlobalSeq) sequence) + " (" + sequence.getLength() + ")";
		} else {
			this.name = "Animation " + ((Animation) sequence).getName();
		}

		for (AnimFlag<?> animFlag : ModelUtils.getAllAnimFlags(model)) {
			if (animFlag.hasSequence(sequence)) {
				removeFlagEntryMapActions.add(new RemoveFlagEntryMapAction<>(animFlag, sequence, null));
			}
		}

		for (EventObject eventObject : model.getEvents()) {
			TreeSet<Integer> eventTrack1 = eventObject.getEventTrack(sequence);
			if (eventTrack1 != null) {
				eventTracksToRemove.put(eventObject, eventTrack1);
			}
		}
	}

	@Override
	public UndoAction undo() {
		if (sequence instanceof GlobalSeq) {
			model.add((GlobalSeq) sequence);
		} else {
			model.add((Animation) sequence);
		}
		for (RemoveFlagEntryMapAction<?> action : removeFlagEntryMapActions) {
			action.undo();
		}
		for (EventObject eventObject : eventTracksToRemove.keySet()) {
			eventObject.setSequence(sequence, eventTracksToRemove.get(eventObject));
		}
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		if (sequence instanceof GlobalSeq) {
			model.remove((GlobalSeq) sequence);
		} else {
			model.remove((Animation) sequence);
		}
		for (RemoveFlagEntryMapAction<?> action : removeFlagEntryMapActions) {
			action.redo();
		}
		for (EventObject eventObject : eventTracksToRemove.keySet()) {
			eventObject.removeSequence(sequence);
		}
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Deleted " + name;
	}
}
