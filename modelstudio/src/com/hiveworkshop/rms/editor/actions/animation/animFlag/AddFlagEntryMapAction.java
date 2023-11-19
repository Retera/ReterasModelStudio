package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.TreeMap;

public class AddFlagEntryMapAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Sequence sequence;
	private final AnimFlag<T> animFlag;
	private final TreeMap<Integer, Entry<T>> entryMap;

	public AddFlagEntryMapAction(AnimFlag<T> animFlag, Sequence sequence, TreeMap<Integer, Entry<T>> entryMap, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.sequence = sequence;
		this.animFlag = animFlag;
		this.entryMap = entryMap;
	}

	@Override
	public AddFlagEntryMapAction<T> undo() {
		animFlag.deleteAnim(sequence);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public AddFlagEntryMapAction<T> redo() {
		animFlag.setEntryMap(sequence, entryMap);
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
