package com.hiveworkshop.rms.ui.application.actions.model.animFlag;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class ChangeFlagEntryAction implements UndoAction {
	private final ModelStructureChangeListener structureChangeListener;
	private final TimelineContainer timelineContainer;
	private final AnimFlag<?> animFlag;
	AnimFlag.Entry<?> entry;
	int orgTime;
	int time;
	AnimFlag.Entry<?> orgEntry;


	public ChangeFlagEntryAction(AnimFlag<?> animFlag, AnimFlag.Entry<?> entry, int orgTime, TimelineContainer timelineContainer, ModelStructureChangeListener structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.timelineContainer = timelineContainer;
		this.animFlag = animFlag;
		this.entry = entry;
		this.orgTime = orgTime;
		time = entry.time;
		int index = animFlag.getTimes().indexOf(orgTime);
		orgEntry = animFlag.getEntry(index);
	}

	@Override
	public void undo() {
		animFlag.setEntryT(time, orgEntry);
		animFlag.sort();
		structureChangeListener.materialsListChanged();
	}

	@Override
	public void redo() {
		animFlag.setEntryT(orgTime, entry);
		animFlag.sort();
		structureChangeListener.materialsListChanged();
	}

	@Override
	public String actionName() {
		return "change animation value";
	}
}
