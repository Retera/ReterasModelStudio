package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
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
	private final Sequence animation;
	private final AnimFlag<T> animFlag;
	private final List<Entry<T>> entries = new ArrayList<>();
	private final GlobalSeq globalSeq;

	public RemoveFlagEntryAction(AnimFlag<T> animFlag, int orgTime, Sequence animation, ModelStructureChangeListener changeListener) {
		this(animFlag, Collections.singleton(orgTime), animation, changeListener);
	}

	public RemoveFlagEntryAction(AnimFlag<T> animFlag, Collection<Integer> orgTimes, Sequence animation, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animation = animation;
		this.animFlag = animFlag;
		this.globalSeq = animFlag.getGlobalSeq();
		for (Integer orgTime : orgTimes) {
			entries.add(animFlag.getEntryAt(animation, orgTime));
		}
	}

	@Override
	public RemoveFlagEntryAction<T> undo() {
		for (Entry<T> entry : entries) {
			animFlag.addEntry(entry.time, entry, animation);
		}
		animFlag.setGlobSeq(globalSeq);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public RemoveFlagEntryAction<T> redo() {
		for (Entry<T> entry : entries) {
			animFlag.removeKeyframe(entry.time, animation);
		}
		if (animation instanceof GlobalSeq && animFlag.getEntryMap(animation).isEmpty()) {
			animFlag.setGlobSeq(globalSeq);
		}
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Delete Keyframe" + (entries.size() == 1 ? "" : "s");
	}
}
