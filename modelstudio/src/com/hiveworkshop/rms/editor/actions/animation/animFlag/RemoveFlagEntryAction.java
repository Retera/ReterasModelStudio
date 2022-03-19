package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RemoveFlagEntryAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private Sequence animation;
	private AnimFlag<T> animFlag;
	private List<Entry<T>> entries = new ArrayList<>();

	public RemoveFlagEntryAction(AnimFlag<T> animFlag, int orgTime, Sequence animation, ModelStructureChangeListener changeListener) {
		this(animFlag, Collections.singleton(orgTime), animation, changeListener);
	}

	public RemoveFlagEntryAction(AnimFlag<T> animFlag, Collection<Integer> orgTimes, Sequence animation, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animation = animation;
		this.animFlag = animFlag;
		for(Integer orgTime : orgTimes){
			entries.add(animFlag.getEntryAt(animation, orgTime));
		}
	}

	@Override
	public UndoAction undo() {
		for(Entry<T> entry : entries){
			animFlag.setOrAddEntry(entry.time, entry, animation);
		}
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for(Entry<T> entry : entries){
			animFlag.removeKeyframe(entry.time, animation);
		}
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "delete keyframe" + (entries.size() == 1 ? "" : "s");
	}
}
