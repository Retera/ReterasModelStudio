package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class ChangeInterpTypeAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final AnimFlag<T> animFlag;
	private final InterpolationType newInterpType;
	private final InterpolationType oldInterpType;
	private final AnimFlag<T> oldAnimFlag;


	public ChangeInterpTypeAction(AnimFlag<T> animFlag, InterpolationType newInterpType, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animFlag = animFlag;
		this.newInterpType = newInterpType;
		oldInterpType = animFlag.getInterpolationType();
		oldAnimFlag = animFlag.deepCopy();
	}

	@Override
	public UndoAction undo() {
		animFlag.setInterpType(oldInterpType);
		animFlag.setSequenceMap(oldAnimFlag.getAnimMap());
//		animFlag.setFromOther(oldAnimFlag);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animFlag.setInterpType(newInterpType);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "change interpolation type";
	}
}
