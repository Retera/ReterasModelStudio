package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class AddAnimFlagAction implements UndoAction {
	private final ModelStructureChangeListener structureChangeListener;
	TimelineContainer timelineContainer;
	AnimFlag<?> animFlag;

	public AddAnimFlagAction(TimelineContainer timelineContainer, AnimFlag<?> animFlag, ModelStructureChangeListener structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.timelineContainer = timelineContainer;
		this.animFlag = animFlag;
	}

//	public AddAnimFlagAction(TimelineContainer timelineContainer, String flagName, ModelStructureChangeListener structureChangeListener) {
//		this.structureChangeListener = structureChangeListener;
//		this.timelineContainer = timelineContainer;
//		animFlag = new AnimFlag(flagName);
//		animFlag.addEntry(0, 1);
//	}

	@Override
	public UndoAction undo() {
		timelineContainer.remove(animFlag);
		if (structureChangeListener != null) {
			structureChangeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		timelineContainer.add(animFlag);
		if (structureChangeListener != null) {
			structureChangeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set dynamic";
	}
}
