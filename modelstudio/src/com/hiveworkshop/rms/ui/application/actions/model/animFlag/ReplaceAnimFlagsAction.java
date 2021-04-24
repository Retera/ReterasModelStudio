package com.hiveworkshop.rms.ui.application.actions.model.animFlag;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

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
			animFlags.add(AnimFlag.createFromAnimFlag(animFlag));
		}
		oldAnimFlags = timelineContainer.getAnimFlags();
	}

//	public AddAnimFlagAction(TimelineContainer timelineContainer, String flagName, ModelStructureChangeListener structureChangeListener) {
//		this.structureChangeListener = structureChangeListener;
//		this.timelineContainer = timelineContainer;
//		animFlag = new AnimFlag(flagName);
//		animFlag.addEntry(0, 1);
//	}

	@Override
	public void undo() {
		timelineContainer.setAnimFlags(oldAnimFlags);
		structureChangeListener.materialsListChanged();

	}

	@Override
	public void redo() {
		timelineContainer.setAnimFlags(animFlags);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public String actionName() {
		return "set dynamic";
	}
}
