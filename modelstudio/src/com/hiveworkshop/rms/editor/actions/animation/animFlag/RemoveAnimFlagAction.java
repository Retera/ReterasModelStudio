package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class RemoveAnimFlagAction implements UndoAction {
	private final ModelStructureChangeListener structureChangeListener;
	TimelineContainer timelineContainer;
	AnimFlag animFlag;

	public RemoveAnimFlagAction(TimelineContainer timelineContainer, AnimFlag animFlag, ModelStructureChangeListener structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.timelineContainer = timelineContainer;
		this.animFlag = animFlag;
	}

	@Override
	public UndoAction undo() {
		timelineContainer.add(animFlag);
		if (structureChangeListener != null) {
			structureChangeListener.materialsListChanged();
		}
		return this;

	}

	@Override
	public UndoAction redo() {
		timelineContainer.remove(animFlag);
		if (structureChangeListener != null) {
			structureChangeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set static";
	}
}
