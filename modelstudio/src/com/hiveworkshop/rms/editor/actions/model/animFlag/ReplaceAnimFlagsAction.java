package com.hiveworkshop.rms.editor.actions.model.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.ArrayList;
import java.util.List;

public class ReplaceAnimFlagsAction implements UndoAction {
	private final ModelStructureChangeListener structureChangeListener;
	TimelineContainer timelineContainer;
	List<AnimFlag<?>> animFlags;
	List<AnimFlag<?>> oldAnimFlags;

	public ReplaceAnimFlagsAction(TimelineContainer timelineContainer, List<AnimFlag<?>> animFlagsToCopy, ModelStructureChangeListener structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
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
		structureChangeListener.materialsListChanged();
		return this;
	}

	@Override
	public UndoAction redo() {
		timelineContainer.setAnimFlags(animFlags);
		structureChangeListener.materialsListChanged();
		return this;
	}

	@Override
	public String actionName() {
		return "set dynamic";
	}
}
