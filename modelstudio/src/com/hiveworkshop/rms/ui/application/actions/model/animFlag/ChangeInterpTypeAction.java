package com.hiveworkshop.rms.ui.application.actions.model.animFlag;

import com.hiveworkshop.rms.editor.model.animflag.*;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class ChangeInterpTypeAction implements UndoAction {
	private final ModelStructureChangeListener structureChangeListener;
	private final AnimFlag<?> animFlag;
	InterpolationType newInterpType;
	InterpolationType oldInterpType;
	AnimFlag<?> oldAnimFlag;


	public ChangeInterpTypeAction(AnimFlag<?> animFlag, InterpolationType newInterpType, ModelStructureChangeListener structureChangeListener) {
		this.structureChangeListener = structureChangeListener;
		this.animFlag = animFlag;
		this.newInterpType = newInterpType;
		oldInterpType = animFlag.getInterpolationType();
		oldAnimFlag = AnimFlag.createFromAnimFlag(animFlag);
	}

	@Override
	public void undo() {
		animFlag.setInterpType(oldInterpType);
		if (oldAnimFlag.getInterpolationType().tangential()) {
			if (oldAnimFlag instanceof IntAnimFlag && animFlag instanceof IntAnimFlag) {
				((IntAnimFlag) animFlag)
						.setInTans(((IntAnimFlag) oldAnimFlag).getInTans())
						.setOutTans(((IntAnimFlag) oldAnimFlag).getOutTans());
			}
			if (oldAnimFlag instanceof FloatAnimFlag && animFlag instanceof FloatAnimFlag) {
				((FloatAnimFlag) animFlag)
						.setInTans(((FloatAnimFlag) oldAnimFlag).getInTans())
						.setOutTans(((FloatAnimFlag) oldAnimFlag).getOutTans());
			}
			if (oldAnimFlag instanceof Vec3AnimFlag && animFlag instanceof Vec3AnimFlag) {
				((Vec3AnimFlag) animFlag)
						.setInTans(((Vec3AnimFlag) oldAnimFlag).getInTans())
						.setOutTans(((Vec3AnimFlag) oldAnimFlag).getOutTans());
			}
			if (oldAnimFlag instanceof QuatAnimFlag && animFlag instanceof QuatAnimFlag) {
				((QuatAnimFlag) animFlag)
						.setInTans(((QuatAnimFlag) oldAnimFlag).getInTans())
						.setOutTans(((QuatAnimFlag) oldAnimFlag).getOutTans());
			}
		}
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
