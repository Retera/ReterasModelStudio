package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.List;

public class ReplaceAnimFlagsAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	TimelineContainer timelineContainer;
	List<AnimFlag<?>> newAnimFlags;
	List<AnimFlag<?>> oldAnimFlags;

	public ReplaceAnimFlagsAction(TimelineContainer timelineContainer, List<AnimFlag<?>> newAnimFlags, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.timelineContainer = timelineContainer;
		this.newAnimFlags = newAnimFlags;
		oldAnimFlags = timelineContainer.getAnimFlags();
	}

	@Override
	public UndoAction undo() {
		timelineContainer.setAnimFlags(oldAnimFlags);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		timelineContainer.setAnimFlags(newAnimFlags);
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
