package com.hiveworkshop.rms.ui.application.actions.model.animFlag;

import com.hiveworkshop.rms.editor.model.animflag.*;
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
		if (animFlag instanceof IntAnimFlag) {
			oldAnimFlag = (AnimFlag<T>) new IntAnimFlag((IntAnimFlag) animFlag);
		} else if (animFlag instanceof FloatAnimFlag) {
			oldAnimFlag = (AnimFlag<T>) new FloatAnimFlag((FloatAnimFlag) animFlag);
		} else if (animFlag instanceof Vec3AnimFlag) {
			oldAnimFlag = (AnimFlag<T>) new Vec3AnimFlag((Vec3AnimFlag) animFlag);
		} else if (animFlag instanceof QuatAnimFlag) {
			oldAnimFlag = (AnimFlag<T>) new QuatAnimFlag((QuatAnimFlag) animFlag);
		}
	}

	@Override
	public void undo() {
		animFlag.setInterpType(oldInterpType);
		animFlag.setEntryMap(oldAnimFlag.getEntryMap());
		animFlag.setFromOther(oldAnimFlag);
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
