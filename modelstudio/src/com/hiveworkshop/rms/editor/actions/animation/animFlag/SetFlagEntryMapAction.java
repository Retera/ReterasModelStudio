package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.TreeMap;

public class SetFlagEntryMapAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Sequence animation;
	private final AnimFlag<T> animFlag;
	private final TreeMap<Integer, Entry<T>> oldEntryMap;
	private final TreeMap<Integer, Entry<T>> newEntryMap;

	public SetFlagEntryMapAction(AnimFlag<T> animFlag, Sequence animation, TreeMap<Integer, Entry<T>> newEntryMap, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animFlag = animFlag;
		this.oldEntryMap = animFlag.getEntryMap(animation);
		this.animation = animation;
		this.newEntryMap = newEntryMap;
	}

	@Override
	public UndoAction undo() {
		if (oldEntryMap == null){
			animFlag.deleteAnim(animation);
		} else {
			animFlag.setEntryMap(animation, oldEntryMap);
		}
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animFlag.setEntryMap(animation, newEntryMap);
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
