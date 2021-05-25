package com.hiveworkshop.rms.ui.application.actions.model.animFlag;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class ChangeFlagEntryAction<T> implements UndoAction {
	private final ModelStructureChangeListener structureChangeListener;
	private final TimelineContainer timelineContainer;
	Entry<T> newEntry;
	int orgTime;
	int time;
	Entry<T> orgEntry;
	Entry<T> entry;


	public ChangeFlagEntryAction(AnimFlag<T> animFlag, Entry<T> newEntry, int orgTime, TimelineContainer timelineContainer, ModelStructureChangeListener structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.timelineContainer = timelineContainer;
		this.newEntry = newEntry;
		this.orgTime = orgTime;
		time = newEntry.time;
		orgEntry = animFlag.getEntryAt(orgTime).deepCopy();
		entry = animFlag.getEntryAt(orgTime);
	}

	@Override
	public void undo() {
		entry.set(orgEntry);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public void redo() {
		entry.set(newEntry);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public String actionName() {
		return "change animation value";
	}
}
