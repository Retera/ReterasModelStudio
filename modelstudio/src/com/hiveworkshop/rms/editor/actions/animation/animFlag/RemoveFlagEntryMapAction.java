package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.TreeMap;

public class RemoveFlagEntryMapAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Sequence animation;
	private final AnimFlag<T> animFlag;
	private final TreeMap<Integer, Entry<T>> entryMap;
	private final String actionName;

	public RemoveFlagEntryMapAction(AnimFlag<T> animFlag, Sequence animation, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animation = animation;
		this.animFlag = animFlag;
		this.entryMap = animFlag.getEntryMap(animation);
		actionName = "Clear data in \"" + animation.getName() + "\"";
	}

	@Override
	public UndoAction undo() {
		animFlag.setEntryMap(animation, entryMap);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animFlag.deleteAnim(animation);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return actionName;
	}
}
