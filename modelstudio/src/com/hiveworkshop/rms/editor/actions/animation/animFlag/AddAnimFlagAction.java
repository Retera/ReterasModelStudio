package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class AddAnimFlagAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final TimelineContainer timelineContainer;
	private final AnimFlag<T> animFlag;
	private final AnimFlag<?> oldAnimFlag;

	public AddAnimFlagAction(TimelineContainer timelineContainer, AnimFlag<T> animFlag, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.timelineContainer = timelineContainer;
		this.animFlag = animFlag;
		this.oldAnimFlag = timelineContainer.find(animFlag.getName());
	}

	@Override
	public UndoAction undo() {
		timelineContainer.remove(animFlag);
		if(oldAnimFlag != null){
			timelineContainer.add(oldAnimFlag);
		}
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
		return "Set Dynamic";
	}
}
