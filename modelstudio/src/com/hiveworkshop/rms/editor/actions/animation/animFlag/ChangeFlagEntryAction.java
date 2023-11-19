package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

public class ChangeFlagEntryAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Sequence sequence;
	private final Entry<T> newEntry;
	private final Entry<T> orgEntry;
	private Entry<T> orgEntryAtNewTime;
	private final AnimFlag<T> animFlag;


	public ChangeFlagEntryAction(AnimFlag<T> animFlag, Entry<T> newEntry, int orgTime, Sequence sequence, ModelStructureChangeListener changeListener) {
		this(animFlag, newEntry, animFlag.getEntryAt(sequence, orgTime), sequence, changeListener);
	}

	public ChangeFlagEntryAction(AnimFlag<T> animFlag, Entry<T> newEntry, Entry<T> oldEntry, Sequence sequence, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.sequence = sequence;
		this.newEntry = newEntry;
		this.animFlag = animFlag;
		this.orgEntry = oldEntry;
		if (!newEntry.getTime().equals(oldEntry.getTime())) {
			orgEntryAtNewTime = animFlag.getEntryAt(sequence, newEntry.getTime());
		}
	}

	@Override
	public ChangeFlagEntryAction<T> undo() {
		animFlag.removeKeyframe(newEntry.getTime(), sequence);
		animFlag.addEntry(orgEntry, sequence);
		if (orgEntryAtNewTime != null) {
			animFlag.addEntry(orgEntryAtNewTime, sequence);
		}
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public ChangeFlagEntryAction<T> redo() {
		animFlag.removeKeyframe(orgEntry.getTime(), sequence);
		animFlag.addEntry(newEntry, sequence);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Change Animation Value";
	}
}
