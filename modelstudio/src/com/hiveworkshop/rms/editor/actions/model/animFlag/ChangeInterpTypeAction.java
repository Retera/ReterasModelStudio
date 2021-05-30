package com.hiveworkshop.rms.editor.actions.model.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class ChangeInterpTypeAction<T> implements UndoAction {
	private final ModelStructureChangeListener structureChangeListener;
	private final AnimFlag<T> animFlag;
	InterpolationType newInterpType;
	InterpolationType oldInterpType;
	AnimFlag<T> oldAnimFlag;


	public ChangeInterpTypeAction(AnimFlag<T> animFlag, InterpolationType newInterpType, ModelStructureChangeListener structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.animFlag = animFlag;
		this.newInterpType = newInterpType;
		oldInterpType = animFlag.getInterpolationType();
		oldAnimFlag = animFlag.deepCopy();
	}

	@Override
	public UndoAction undo() {
		animFlag.setInterpType(oldInterpType);
		animFlag.setEntryMap(oldAnimFlag.getEntryMap());
//		animFlag.setFromOther(oldAnimFlag);
		structureChangeListener.materialsListChanged();
		return this;
	}

	@Override
	public UndoAction redo() {
		animFlag.setInterpType(newInterpType);
		structureChangeListener.materialsListChanged();
		return this;
	}

	@Override
	public String actionName() {
		return "change interpolation type";
	}
}
