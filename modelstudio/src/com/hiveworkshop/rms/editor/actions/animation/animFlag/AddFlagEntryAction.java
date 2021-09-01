package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

public class AddFlagEntryAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	Sequence animation;
	AnimFlag<T> animFlag;
	Entry<T> entry;

	public AddFlagEntryAction(AnimFlag<T> animFlag, Entry<T> entry, Sequence animation, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animation = animation;
		this.animFlag = animFlag;
		this.entry = entry;
	}

	@Override
	public UndoAction undo() {
		animFlag.removeKeyframe(entry.time, animation);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animFlag.setOrAddEntry(entry.time, entry, animation);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "add animation";
	}
}
