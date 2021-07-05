package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.ArrayList;
import java.util.List;

public class ReplaceAnimFlagsAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	TimelineContainer timelineContainer;
	List<AnimFlag<?>> animFlags;
	List<AnimFlag<?>> oldAnimFlags;

	public ReplaceAnimFlagsAction(TimelineContainer timelineContainer, List<AnimFlag<?>> animFlagsToCopy, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.timelineContainer = timelineContainer;
		this.animFlags = new ArrayList<>();
		for (AnimFlag<?> animFlag : animFlagsToCopy) {
			animFlags.add(animFlag.deepCopy());
		}
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
		timelineContainer.setAnimFlags(animFlags);
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
