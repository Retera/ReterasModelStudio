package com.hiveworkshop.rms.ui.application.actions.model.material;

import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class RemoveMaterialAction implements UndoAction {
	private final Material material;
	private final ModelViewManager modelViewManager;
	private final ModelStructureChangeListener structureChangeListener;

	public RemoveMaterialAction(final Material material,
	                            final ModelViewManager modelViewManager,
	                            final ModelStructureChangeListener modelStructureChangeListener) {
		this.material = new Material(material);
		this.modelViewManager = modelViewManager;
		this.structureChangeListener = modelStructureChangeListener;
	}

	@Override
	public void undo() {
		modelViewManager.getModel().addMaterial(material);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public void redo() {
		modelViewManager.getModel().getMaterials().remove(material);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public String actionName() {
		return "remove Material";
	}

}
