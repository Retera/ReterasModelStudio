package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class RemoveFlagEntryAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	AnimFlag<?> animFlag;
	Entry<?> entry;

	public RemoveFlagEntryAction(AnimFlag<?> animFlag, int orgTime, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animFlag = animFlag;
		this.entry = animFlag.getEntryAt(orgTime);
	}

	@Override
	public UndoAction undo() {
		animFlag.setOrAddEntryT(entry.time, entry);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animFlag.removeKeyframe(entry.time);
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
