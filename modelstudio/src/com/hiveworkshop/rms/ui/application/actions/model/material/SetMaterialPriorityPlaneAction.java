package com.hiveworkshop.rms.ui.application.actions.model.material;

import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class SetMaterialPriorityPlaneAction implements UndoAction {
	private final Material material;
	private final int prevPriorityPlane;
	private final int newPriorityPlane;
	private final ModelStructureChangeListener modelStructureChangeListener;

	public SetMaterialPriorityPlaneAction(final Material material, final int prevPriorityPlane,
			final int newPriorityPlane, final ModelStructureChangeListener modelStructureChangeListener) {
		this.material = material;
		this.prevPriorityPlane = prevPriorityPlane;
		this.newPriorityPlane = newPriorityPlane;
		this.modelStructureChangeListener = modelStructureChangeListener;
	}

	@Override
	public UndoAction undo() {
		material.setPriorityPlane(prevPriorityPlane);
		modelStructureChangeListener.texturesChanged();
		return this;
	}

	@Override
	public UndoAction redo() {
		material.setPriorityPlane(newPriorityPlane);
		modelStructureChangeListener.texturesChanged();
		return this;
	}

	@Override
	public String actionName() {
		return "set material PriorityPlane";
	}

}
