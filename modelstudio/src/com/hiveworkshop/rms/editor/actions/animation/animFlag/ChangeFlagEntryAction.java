package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class ChangeFlagEntryAction<T> implements UndoAction {
	private final ModelStructureChangeListener structureChangeListener;
	Entry<T> newEntry;
	Entry<T> orgEntry;
	AnimFlag<T> animFlag;


	public ChangeFlagEntryAction(AnimFlag<T> animFlag, Entry<T> newEntry, int orgTime, ModelStructureChangeListener structureChangeListener) {
		this(animFlag, newEntry, animFlag.getEntryAt(orgTime), structureChangeListener);
	}

	public ChangeFlagEntryAction(AnimFlag<T> animFlag, Entry<T> newEntry, Entry<T> oldEntry, ModelStructureChangeListener structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.newEntry = newEntry;
		this.animFlag = animFlag;
		orgEntry = oldEntry;
	}

	@Override
	public UndoAction undo() {
		animFlag.changeEntryAt(orgEntry.getTime(), orgEntry);
		if (structureChangeListener != null) {
			structureChangeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animFlag.changeEntryAt(orgEntry.getTime(), newEntry);
		if (structureChangeListener != null) {
			structureChangeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "change animation value";
	}
}
