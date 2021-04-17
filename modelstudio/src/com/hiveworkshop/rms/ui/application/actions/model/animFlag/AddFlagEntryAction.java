package com.hiveworkshop.rms.ui.application.actions.model.animFlag;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class AddFlagEntryAction implements UndoAction {
	private final ModelStructureChangeListener structureChangeListener;
	private final TimelineContainer timelineContainer;
	AnimFlag animFlag;
	AnimFlag.Entry entry;

	public AddFlagEntryAction(AnimFlag animFlag, AnimFlag.Entry entry, TimelineContainer timelineContainer, ModelStructureChangeListener structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.timelineContainer = timelineContainer;
		this.animFlag = animFlag;
		this.entry = entry;
	}

	public AddFlagEntryAction(AnimFlag animFlag, int time, Object value, TimelineContainer timelineContainer, ModelStructureChangeListener structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.timelineContainer = timelineContainer;
		this.animFlag = animFlag;
		entry = new AnimFlag.Entry(time, value);
	}

	public AddFlagEntryAction(AnimFlag animFlag, int time, Object value, Object inTan, Object outTan, TimelineContainer timelineContainer, ModelStructureChangeListener structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.timelineContainer = timelineContainer;
		this.animFlag = animFlag;
		entry = new AnimFlag.Entry(time, value, inTan, outTan);
	}

	@Override
	public void undo() {
		animFlag.removeKeyframe(entry.time);
//		structureChangeListener.keyframeRemoved(timelineContainer, animFlag, entry.time);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public void redo() {
		animFlag.addEntry(entry);
		animFlag.sort();
//		structureChangeListener.keyframeAdded(timelineContainer, animFlag, entry.time);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public String actionName() {
		return "add animation";
	}
}
