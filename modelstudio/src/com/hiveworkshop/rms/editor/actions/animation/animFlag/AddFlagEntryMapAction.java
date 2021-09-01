package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.TreeMap;

public class AddFlagEntryMapAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	Sequence animation;
	AnimFlag<T> animFlag;
	TreeMap<Integer, Entry<T>> entryMap;

	public AddFlagEntryMapAction(AnimFlag<T> animFlag, Sequence animation, TreeMap<Integer, Entry<T>> entryMap, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animation = animation;
		this.animFlag = animFlag;
		this.entryMap = entryMap;
	}

	@Override
	public UndoAction undo() {
		animFlag.deleteAnim(animation);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animFlag.setEntryMap(animation, entryMap);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "add animation";
	}
}
