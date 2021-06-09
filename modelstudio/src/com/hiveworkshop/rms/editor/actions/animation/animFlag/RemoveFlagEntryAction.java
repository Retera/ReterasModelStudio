package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class RemoveFlagEntryAction implements UndoAction {
	private final ModelStructureChangeListener structureChangeListener;
	AnimFlag<?> animFlag;
	Entry<?> entry;

	public RemoveFlagEntryAction(AnimFlag<?> animFlag, int orgTime, TimelineContainer timelineContainer, ModelStructureChangeListener structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.animFlag = animFlag;
		this.entry = animFlag.getEntryAt(orgTime);
	}

	@Override
	public UndoAction undo() {
		animFlag.setOrAddEntryT(entry.time, entry);
		if (structureChangeListener != null) {
			structureChangeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animFlag.removeKeyframe(entry.time);
		if (structureChangeListener != null) {
			structureChangeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "delete keyframe";
	}
}
