package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

public class OffsetSequenceAction implements UndoAction {
	ModelStructureChangeListener changeListener;
	List<AnimFlag<?>> animFlags;
	Sequence sequence;
	int offset;

	public OffsetSequenceAction(Collection<AnimFlag<?>> animFlags, Sequence sequence, float offset,
	                            ModelStructureChangeListener changeListener) {
		this(animFlags, sequence, (int)(sequence.getLength()*offset), changeListener);
	}

	public OffsetSequenceAction(Collection<AnimFlag<?>> animFlags, Sequence sequence, int offset,
	                            ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animFlags = new ArrayList<>();
		this.sequence = sequence;
		this.offset = offset;

		for (AnimFlag<?> animFlag : animFlags) {
			if (animFlag.getEntryMap(sequence) != null && 0 < animFlag.getEntryMap(sequence).size()) {
				this.animFlags.add(animFlag);
			}
		}
	}

	private <Q> void doOffset1(AnimFlag<Q> animFlag) {
		TreeMap<Integer, Entry<Q>> tempMap = new TreeMap<>();
		TreeMap<Integer, Entry<Q>> entryMap = animFlag.getEntryMap(sequence);
		for (Integer i : entryMap.keySet()) {
			Entry<Q> value = entryMap.get(i);
			int newTime = (i + offset) % (sequence.getLength()+1);
			tempMap.put(newTime, value);
//			value.time = newTime;
		}
		entryMap.clear();
		for (Integer i : tempMap.keySet()) {
			Entry<Q> value = tempMap.get(i);
			int newTime = (i + offset) % sequence.getLength();
			tempMap.put(newTime, value);
//			value.time = newTime;
		}
//		animFlag.changeEntryAt();
	}

	private <Q> void doOffset(AnimFlag<Q> animFlag, int offset) {
		TreeMap<Integer, Entry<Q>> newMap = new TreeMap<>();
		TreeMap<Integer, Entry<Q>> entryMap = animFlag.getEntryMap(sequence);
		for (Integer i : entryMap.keySet()) {
			Entry<Q> value = entryMap.get(i);
			value.time = (i + offset) % (sequence.getLength()+1);
			newMap.put(value.time, value);
		}
		animFlag.setEntryMap(sequence, newMap);
	}

	@Override
	public OffsetSequenceAction undo() {
		for (AnimFlag<?> animFlag : animFlags) {
			doOffset(animFlag, -offset);
		}
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public OffsetSequenceAction redo() {
		for (AnimFlag<?> animFlag : animFlags) {
			doOffset(animFlag, offset);
		}
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Offset \"" + "Sequence" + "\"";
	}
}
