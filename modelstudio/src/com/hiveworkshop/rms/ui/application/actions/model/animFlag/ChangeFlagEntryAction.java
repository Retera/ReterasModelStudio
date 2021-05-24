package com.hiveworkshop.rms.ui.application.actions.model.animFlag;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class ChangeFlagEntryAction implements UndoAction {
	private final ModelStructureChangeListener structureChangeListener;
	private final TimelineContainer timelineContainer;
	private final AnimFlag<?> animFlag;
	Entry<?> entry;
	int orgTime;
	int time;
	Entry<?> orgEntry;


	public ChangeFlagEntryAction(AnimFlag<?> animFlag, Entry<?> entry, int orgTime, TimelineContainer timelineContainer, ModelStructureChangeListener structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.timelineContainer = timelineContainer;
		this.animFlag = animFlag;
		this.entry = entry;
		this.orgTime = orgTime;
		time = entry.time;
		orgEntry = animFlag.getEntryAt(orgTime);
	}

	@Override
	public void undo() {
		animFlag.setEntryT(time, orgEntry);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public void redo() {
		animFlag.setEntryT(orgTime, entry);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public String actionName() {
		return "change animation value";
	}
}
