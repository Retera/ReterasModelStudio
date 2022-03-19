package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetFlagFromOtherAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final AnimFlag<T> animFlag;
	private final AnimFlag<T> oldFlag;
	private final AnimFlag<T> otherFlag;

	public SetFlagFromOtherAction(AnimFlag<T> animFlag, AnimFlag<T> otherFlag, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animFlag = animFlag;
		this.oldFlag = animFlag.deepCopy();
		this.otherFlag = otherFlag.deepCopy();
	}

	@Override
	public UndoAction undo() {
		animFlag.setFromOther(oldFlag);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animFlag.setFromOther(otherFlag);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set from other";
	}
}
