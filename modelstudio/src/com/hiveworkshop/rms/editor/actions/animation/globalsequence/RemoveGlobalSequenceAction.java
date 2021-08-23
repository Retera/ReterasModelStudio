package com.hiveworkshop.rms.editor.actions.animation.globalsequence;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.*;

public class RemoveGlobalSequenceAction implements UndoAction {
	private final EditableModel model;
	private final GlobalSeq globalSeq;
	private final ModelStructureChangeListener changeListener;
	private final Map<AnimFlag<?>, Set<Entry<?>>> entriesToRemove = new HashMap<>();
	private final Map<EventObject, Set<Integer>> eventTracksToRemove = new HashMap<>();

	public RemoveGlobalSequenceAction(EditableModel model, GlobalSeq globalSeq, ModelStructureChangeListener changeListener, boolean clearKeyframes) {
		this.model = model;
		this.globalSeq = globalSeq;
		this.changeListener = changeListener;

		if (clearKeyframes) {
			for (AnimFlag<?> animFlag : model.getAllAnimFlags()) {
				if (animFlag.getGlobalSeq() == globalSeq) {
					TreeMap<Integer, ? extends Entry<?>> entryMap = animFlag.getEntryMap();
					NavigableSet<Integer> times = entryMap.navigableKeySet();
					Set<Entry<?>> entrySet = new HashSet<>();
					Integer firstKfTime = times.ceiling(globalSeq.getStart());
					Integer lastKfTime = times.floor(globalSeq.getEnd());
					for (Integer i = firstKfTime; i != null && lastKfTime != null && i <= lastKfTime; i = times.higher(i)) {
						entrySet.add(animFlag.getEntryAt(i));
					}
					entriesToRemove.put(animFlag, entrySet);
				}
			}
			for (EventObject eventObject : model.getEvents()) {
				if (eventObject.getGlobalSeq() == globalSeq) {
					TreeSet<Integer> eventTrack = eventObject.getEventTrack();
					Set<Integer> tracksToRemove = new HashSet<>(eventTrack.subSet(globalSeq.getStart(), true, globalSeq.getEnd(), true));
					eventTracksToRemove.put(eventObject, tracksToRemove);
				}
			}
		}
	}

	@Override
	public UndoAction undo() {
		model.add(globalSeq);
		for (AnimFlag<?> animFlag : entriesToRemove.keySet()) {
			for (Entry<?> entry : entriesToRemove.get(animFlag)) {
				animFlag.setOrAddEntryT(entry.getTime(), entry);
			}
		}
		for (EventObject eventObject : eventTracksToRemove.keySet()) {
			eventObject.addTracks(eventTracksToRemove.get(eventObject));
		}
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		model.remove(globalSeq);
		for (AnimFlag<?> animFlag : entriesToRemove.keySet()) {
			for (Entry<?> entry : entriesToRemove.get(animFlag)) {
				animFlag.removeKeyframe(entry.getTime());
			}
		}
		for (EventObject eventObject : eventTracksToRemove.keySet()) {
			eventObject.removeTracks(eventTracksToRemove.get(eventObject));
		}
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Animation Deleted";
	}
}
