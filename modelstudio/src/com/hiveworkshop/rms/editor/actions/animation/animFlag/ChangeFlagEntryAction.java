package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

public class ChangeFlagEntryAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	Sequence animation;
	Entry<T> newEntry;
	Entry<T> orgEntry;
	AnimFlag<T> animFlag;


	public ChangeFlagEntryAction(AnimFlag<T> animFlag, Entry<T> newEntry, int orgTime, Sequence animation, ModelStructureChangeListener changeListener) {
		this(animFlag, newEntry, animFlag.getEntryAt(animation, orgTime), animation, changeListener);
	}

	public ChangeFlagEntryAction(AnimFlag<T> animFlag, Entry<T> newEntry, Entry<T> oldEntry, Sequence animation, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animation = animation;
		this.newEntry = newEntry;
		this.animFlag = animFlag;
		orgEntry = oldEntry;
	}

	@Override
	public UndoAction undo() {
		animFlag.changeEntryAt(orgEntry.getTime(), orgEntry, animation);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animFlag.changeEntryAt(orgEntry.getTime(), newEntry, animation);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "change animation value";
	}
}
