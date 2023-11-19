package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.Collection;
import java.util.Collections;

public class AddFlagEntryAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Sequence sequence;
	private final AnimFlag<T> animFlag;
	private final Collection<Entry<T>> entries;
	private final GlobalSeq globalSeq;

	public AddFlagEntryAction(AnimFlag<T> animFlag, Entry<T> entry, Sequence sequence, ModelStructureChangeListener changeListener) {
		this(animFlag, Collections.singleton(entry), sequence, changeListener);
	}
	public AddFlagEntryAction(AnimFlag<T> animFlag, Collection<Entry<T>> entries, Sequence sequence, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.sequence = sequence;
		this.animFlag = animFlag;
		this.globalSeq = animFlag.getGlobalSeq();
		this.entries = entries;
	}

	@Override
	public AddFlagEntryAction<T> undo() {
		for (Entry<T> entry : entries) {
			animFlag.removeKeyframe(entry.time, sequence);
		}
		animFlag.setGlobSeq(globalSeq);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public AddFlagEntryAction<T> redo() {
		for (Entry<T> entry : entries) {
			animFlag.addEntry(entry, sequence);
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
