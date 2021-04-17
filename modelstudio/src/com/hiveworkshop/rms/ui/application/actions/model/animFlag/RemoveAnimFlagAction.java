package com.hiveworkshop.rms.ui.application.actions.model.animFlag;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

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
	public void undo() {
		timelineContainer.add(animFlag);
		structureChangeListener.materialsListChanged();

	}

	@Override
	public void redo() {
		timelineContainer.remove(animFlag);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public String actionName() {
		return "set static";
	}
}
