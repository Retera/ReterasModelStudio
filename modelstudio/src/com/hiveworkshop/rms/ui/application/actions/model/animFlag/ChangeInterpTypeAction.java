package com.hiveworkshop.rms.ui.application.actions.model.animFlag;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

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
	public void undo() {
		animFlag.setInterpType(oldInterpType);
		animFlag.setEntryMap(oldAnimFlag.getEntryMap());
//		animFlag.setFromOther(oldAnimFlag);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public void redo() {
		animFlag.setInterpType(newInterpType);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public String actionName() {
		return "change interpolation type";
	}
}
