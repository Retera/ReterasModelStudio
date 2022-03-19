package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

public class ChangeFlagEntryAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Sequence animation;
	private final Entry<T> newEntry;
	private final Entry<T> orgEntry;
	private Entry<T> orgEntryAtNewTime;
	private final AnimFlag<T> animFlag;


	public ChangeFlagEntryAction(AnimFlag<T> animFlag, Entry<T> newEntry, int orgTime, Sequence sequence, ModelStructureChangeListener changeListener) {
		this(animFlag, newEntry, animFlag.getEntryAt(sequence, orgTime), sequence, changeListener);
	}

	public ChangeFlagEntryAction(AnimFlag<T> animFlag, Entry<T> newEntry, Entry<T> oldEntry, Sequence animation, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animation = animation;
		this.newEntry = newEntry;
		this.animFlag = animFlag;
		orgEntry = oldEntry;
		if(!newEntry.getTime().equals(oldEntry.getTime())){
			orgEntryAtNewTime = animFlag.getEntryAt(animation, newEntry.getTime());
		}
	}

	@Override
	public UndoAction undo() {
		animFlag.changeEntryAt(newEntry.getTime(), orgEntry, animation);
		if(orgEntryAtNewTime != null){
			animFlag.addEntry(orgEntryAtNewTime, animation);
		}
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animFlag.changeEntryAt(orgEntry.getTime(), newEntry, animation);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "change animation value";
	}
}
