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
	private final ModelStructureChangeListener changeListener;
	private final List<AnimFlag<?>> animFlags;
	private final Sequence sequence;
	private final int offset;
	private final boolean wrap;
	private final String actionName;

	public OffsetSequenceAction(Collection<AnimFlag<?>> animFlags, Sequence sequence, float offset, ModelStructureChangeListener changeListener) {
		this(animFlags, sequence, (int)(sequence.getLength() * offset), true, changeListener);
	}
	public OffsetSequenceAction(Collection<AnimFlag<?>> animFlags, Sequence sequence, float offset, boolean wrap, ModelStructureChangeListener changeListener) {
		this(animFlags, sequence, (int)(sequence.getLength() * offset), wrap, changeListener);
	}

	public OffsetSequenceAction(Collection<AnimFlag<?>> animFlags, Sequence sequence, int offset, ModelStructureChangeListener changeListener) {
		this(animFlags, sequence, offset, true, changeListener);
	}
	public OffsetSequenceAction(Collection<AnimFlag<?>> animFlags, Sequence sequence, int offset, boolean wrap, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animFlags = new ArrayList<>();
		this.sequence = sequence;
		this.offset = offset;
		this.wrap = wrap;

		for (AnimFlag<?> animFlag : animFlags) {
			if (animFlag.getEntryMap(sequence) != null && 0 < animFlag.getEntryMap(sequence).size()) {
				this.animFlags.add(animFlag);
			}
		}

		actionName = "Offset \"" + sequence.getName() + "\" by " + offset + " frames for " + animFlags.size() + " timelines";
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
			value.time = getNewValidTime(i + offset);
			newMap.put(value.time, value);
		}
		animFlag.setEntryMap(sequence, newMap);
	}

	private int getNewValidTime(int newTime) {
		if (wrap) {
			for (int i = 0; i < 10; i++) {
				if (newTime < 0) {
					newTime = newTime + sequence.getLength();
				} else if (sequence.getLength() < newTime) {
					newTime = newTime - sequence.getLength();
				} else {
					break;
				}
			}
		}
		return newTime;
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
		return actionName;
	}
}
