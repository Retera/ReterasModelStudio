package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class ChangeFlagEntryAction<T> implements UndoAction {
	private final ModelStructureChangeListener structureChangeListener;
	Entry<T> newEntry;
	int orgTime;
	int time;
	Entry<T> orgEntry;
	Entry<T> entry;
	AnimFlag<T> animFlag;


	public ChangeFlagEntryAction(AnimFlag<T> animFlag, Entry<T> newEntry, int orgTime, TimelineContainer timelineContainer, ModelStructureChangeListener structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.newEntry = newEntry;
		this.orgTime = orgTime;
		this.animFlag = animFlag;
		time = newEntry.time;
		orgEntry = animFlag.getEntryAt(orgTime).deepCopy();
		entry = animFlag.getEntryAt(orgTime);
	}

	@Override
	public UndoAction undo() {
//		entry.set(orgEntry);
//		animFlag.removeKeyframe(newEntry.getTime());
//		animFlag.setOrAddEntryT(entry.getTime(), entry);
		animFlag.changeEntryAt(entry.getTime(), orgEntry);
		if (structureChangeListener != null) {
			structureChangeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animFlag.changeEntryAt(orgEntry.getTime(), newEntry);
//		entry.set(newEntry);
//		animFlag.removeKeyframe(orgTime);
//		animFlag.setOrAddEntryT(entry.getTime(), entry);
		if (structureChangeListener != null) {
			structureChangeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "change animation value";
	}
}
