package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class SetFlagEntryAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Sequence sequence;
	private final AnimFlag<T> animFlag;
	private final Collection<Entry<T>> entries;
	private final Collection<Entry<T>> orgEntries;
	private final GlobalSeq globalSeq;

	public SetFlagEntryAction(AnimFlag<T> animFlag, Entry<T> entry, Sequence sequence, ModelStructureChangeListener changeListener) {
		this(animFlag, Collections.singleton(entry), sequence, changeListener);
	}

	public SetFlagEntryAction(AnimFlag<T> animFlag, Collection<Entry<T>> entries, Sequence sequence, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.sequence = sequence;
		this.animFlag = animFlag;
		this.globalSeq = animFlag.getGlobalSeq();
		this.entries = entries;
		this.orgEntries = new HashSet<>();
		for (Entry<T> entry : entries) {
			if (animFlag.hasEntryAt(sequence, entry.getTime())) {
				orgEntries.add(animFlag.getEntryAt(sequence, entry.getTime()));
			}
		}
	}

	@Override
	public SetFlagEntryAction<T> undo() {
		for (Entry<T> entry : entries) {
			animFlag.removeKeyframe(entry.time, sequence);
		}
		for (Entry<T> entry : orgEntries) {
			animFlag.addEntry(entry.time, entry, sequence);
		}
		animFlag.setGlobSeq(globalSeq);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public SetFlagEntryAction<T> redo() {
		for (Entry<T> entry : entries) {
			animFlag.addEntry(entry.time, entry, sequence);
		}
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Add Keyframe" + (entries.size() == 1 ? "" : "s");
	}
}
