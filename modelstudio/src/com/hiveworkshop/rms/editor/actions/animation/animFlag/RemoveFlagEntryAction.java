package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

public class RemoveFlagEntryAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	Sequence animation;
	AnimFlag<T> animFlag;
	Entry<T> entry;

	public RemoveFlagEntryAction(AnimFlag<T> animFlag, int orgTime, Sequence animation, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animation = animation;
		this.animFlag = animFlag;
		this.entry = animFlag.getEntryAt(animation, orgTime);
	}

	@Override
	public UndoAction undo() {
		animFlag.setOrAddEntry(entry.time, entry, animation);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animFlag.removeKeyframe(entry.time, animation);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "delete keyframe";
	}
}
