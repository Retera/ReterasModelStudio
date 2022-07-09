package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class SetFlagEntryAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Sequence animation;
	private final AnimFlag<T> animFlag;
	private final Collection<Entry<T>> entries;
	private final Collection<Entry<T>> orgEntries;
	private final GlobalSeq globalSeq;

	public SetFlagEntryAction(AnimFlag<T> animFlag, Entry<T> entry, Sequence animation, ModelStructureChangeListener changeListener) {
		this(animFlag, Collections.singleton(entry), animation, changeListener);
	}
	public SetFlagEntryAction(AnimFlag<T> animFlag, Collection<Entry<T>> entries, Sequence animation, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animation = animation;
		this.animFlag = animFlag;
		this.globalSeq = animFlag.getGlobalSeq();
		this.entries = entries;
		orgEntries = new HashSet<>();
		for(Entry<T> entry : entries){
			if(animFlag.hasEntryAt(animation, entry.getTime())){
				orgEntries.add(animFlag.getEntryAt(animation, entry.getTime()));
			}
		}
	}

	@Override
	public UndoAction undo() {
		for(Entry<T> entry : entries){
			animFlag.removeKeyframe(entry.time, animation);
		}
		for(Entry<T> entry : orgEntries){
			animFlag.setOrAddEntry(entry.time, entry, animation);
		}
		animFlag.setGlobSeq(globalSeq);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for(Entry<T> entry : entries){
			animFlag.setOrAddEntry(entry.time, entry, animation);
		}
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "add keyframe" + (entries.size() == 1 ? "" : "s");
	}
}
