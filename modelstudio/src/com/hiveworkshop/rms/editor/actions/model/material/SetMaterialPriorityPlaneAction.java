package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetMaterialPriorityPlaneAction implements UndoAction {
	private final Material material;
	private final int prevPriorityPlane;
	private final int newPriorityPlane;
	private final ModelStructureChangeListener changeListener;

	public SetMaterialPriorityPlaneAction(Material material, int newPriorityPlane, ModelStructureChangeListener changeListener) {
		this.material = material;
		this.prevPriorityPlane = material.getPriorityPlane();
		this.newPriorityPlane = newPriorityPlane;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		material.setPriorityPlane(prevPriorityPlane);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		material.setPriorityPlane(newPriorityPlane);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set material PriorityPlane";
	}

}
