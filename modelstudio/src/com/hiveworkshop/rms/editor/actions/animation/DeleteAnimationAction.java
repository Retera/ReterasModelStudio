package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.*;

public class DeleteAnimationAction implements UndoAction {
	//	private final int prevIntervalStart;
//	private final int newIntervalStart;
	private final EditableModel model;
	private final Animation animation;
	private final ModelStructureChangeListener changeListener;
	Map<AnimFlag<?>, Set<Entry<?>>> entriesToRemove = new HashMap<>();
	Map<EventObject, Set<Integer>> eventTracksToRemove = new HashMap<>();

	public DeleteAnimationAction(EditableModel model, Animation animation, ModelStructureChangeListener changeListener, boolean clearKeyframes) {
		this.model = model;
		this.animation = animation;
		this.changeListener = changeListener;

		if (clearKeyframes) {
			for (AnimFlag<?> animFlag : model.getAllAnimFlags()) {
				TreeMap<Integer, ? extends Entry<?>> entryMap = animFlag.getEntryMap();
				NavigableSet<Integer> times = entryMap.navigableKeySet();
				Set<Entry<?>> entrySet = new HashSet<>();
				Integer firstKfTime = times.ceiling(animation.getStart());
				Integer lastKfTime = times.floor(animation.getEnd());
				for (Integer i = firstKfTime; i != null && lastKfTime != null && i <= lastKfTime; i = times.higher(i)) {
					entrySet.add(animFlag.getEntryAt(i));
				}
				entriesToRemove.put(animFlag, entrySet);
			}
			for (EventObject eventObject : model.getEvents()) {
				TreeSet<Integer> eventTrack = eventObject.getEventTrack();
				Set<Integer> tracksToRemove = new HashSet<>(eventTrack.subSet(animation.getStart(), true, animation.getEnd(), true));
				eventTracksToRemove.put(eventObject, tracksToRemove);
			}
		}
	}

	@Override
	public UndoAction undo() {
		model.addAnimation(animation);
		for (AnimFlag<?> animFlag : entriesToRemove.keySet()) {
			for (Entry<?> entry : entriesToRemove.get(animFlag)) {
				animFlag.setOrAddEntryT(entry.getTime(), entry);
			}
		}
		for (EventObject eventObject : eventTracksToRemove.keySet()) {
			eventObject.addTracks(eventTracksToRemove.get(eventObject));
		}
		if (changeListener != null) {
			changeListener.animationParamsChanged(animation);
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		model.remove(animation);
		for (AnimFlag<?> animFlag : entriesToRemove.keySet()) {
			for (Entry<?> entry : entriesToRemove.get(animFlag)) {
				animFlag.removeKeyframe(entry.getTime());
			}
		}
		for (EventObject eventObject : eventTracksToRemove.keySet()) {
			eventObject.removeTracks(eventTracksToRemove.get(eventObject));
		}
		if (changeListener != null) {
			changeListener.animationParamsChanged(animation);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Animation Deleted";
	}
}
