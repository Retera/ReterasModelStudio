package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.Collection;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SetFlagEntryMapAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Sequence sequence;
	private final AnimFlag<T> animFlag;
	private final TreeMap<Integer, Entry<T>> oldEntryMap;
	private final TreeMap<Integer, Entry<T>> newEntryMap;

	public SetFlagEntryMapAction(AnimFlag<T> animFlag, Sequence sequence, Collection<Entry<T>> newEntryMap, ModelStructureChangeListener changeListener) {
		this(animFlag, sequence, new TreeMap<>(newEntryMap.stream().collect(Collectors.toMap(Entry::getTime, e -> e))), changeListener);
	}

	public SetFlagEntryMapAction(AnimFlag<T> animFlag, Sequence sequence, TreeMap<Integer, Entry<T>> newEntryMap, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animFlag = animFlag;
		this.oldEntryMap = animFlag.getEntryMap(sequence);
		this.sequence = sequence;
		this.newEntryMap = newEntryMap;
	}

	@Override
	public SetFlagEntryMapAction<T> undo() {
		if (oldEntryMap == null) {
			animFlag.deleteAnim(sequence);
		} else {
			animFlag.setEntryMap(sequence, oldEntryMap);
		}
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public SetFlagEntryMapAction<T> redo() {
		animFlag.setEntryMap(sequence, newEntryMap);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Add Animation";
	}
}
