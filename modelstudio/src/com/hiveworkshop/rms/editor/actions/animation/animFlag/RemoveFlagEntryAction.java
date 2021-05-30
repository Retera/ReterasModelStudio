package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class RemoveFlagEntryAction implements UndoAction {
	private final ModelStructureChangeListener structureChangeListener;
	private final TimelineContainer timelineContainer;
	AnimFlag<?> animFlag;
	Entry<?> entry;

	public RemoveFlagEntryAction(AnimFlag<?> animFlag, int orgTime, TimelineContainer timelineContainer, ModelStructureChangeListener structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.timelineContainer = timelineContainer;
		this.animFlag = animFlag;
		this.entry = animFlag.getEntryAt(orgTime);
	}

	@Override
	public UndoAction undo() {
		animFlag.setOrAddEntryT(entry.time, entry);
//		structureChangeListener.keyframeAdded(timelineContainer, animFlag, entry.time);
		structureChangeListener.materialsListChanged();
		return this;
	}

	@Override
	public UndoAction redo() {
		animFlag.removeKeyframe(entry.time);
//		structureChangeListener.keyframeRemoved(timelineContainer, animFlag, entry.time);
		structureChangeListener.materialsListChanged();
		return this;
	}

	@Override
	public String actionName() {
		return "delete keyframe";
	}
}
