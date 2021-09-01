package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class AddAnimFlagAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	TimelineContainer timelineContainer;
	AnimFlag<T> animFlag;

	public AddAnimFlagAction(TimelineContainer timelineContainer, AnimFlag<T> animFlag, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.timelineContainer = timelineContainer;
		this.animFlag = animFlag;
	}

	@Override
	public UndoAction undo() {
		timelineContainer.remove(animFlag);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		timelineContainer.add(animFlag);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set dynamic";
	}
}
