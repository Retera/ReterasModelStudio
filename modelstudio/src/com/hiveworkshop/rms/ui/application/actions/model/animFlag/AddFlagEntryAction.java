package com.hiveworkshop.rms.ui.application.actions.model.animFlag;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class AddFlagEntryAction implements UndoAction {
	private final ModelStructureChangeListener structureChangeListener;
	private final TimelineContainer timelineContainer;
	AnimFlag<?> animFlag;
	Entry<?> entry;

	public AddFlagEntryAction(AnimFlag<?> animFlag, Entry<?> entry, TimelineContainer timelineContainer, ModelStructureChangeListener structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.timelineContainer = timelineContainer;
		this.animFlag = animFlag;
		this.entry = entry;
	}

	@Override
	public void undo() {
		animFlag.removeKeyframe(entry.time);
//		structureChangeListener.keyframeRemoved(timelineContainer, animFlag, entry.time);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public void redo() {
		animFlag.setOrAddEntryT(entry.time, entry);
//		structureChangeListener.keyframeAdded(timelineContainer, animFlag, entry.time);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public String actionName() {
		return "add animation";
	}
}
