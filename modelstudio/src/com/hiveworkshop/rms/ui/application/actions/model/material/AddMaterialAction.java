package com.hiveworkshop.rms.ui.application.actions.model.material;

import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class AddMaterialAction implements UndoAction {
	private final Material material;
	private final ModelViewManager modelViewManager;
	private final ModelStructureChangeListener structureChangeListener;

	public AddMaterialAction(final Material material,
	                         final ModelViewManager modelViewManager,
	                         final ModelStructureChangeListener modelStructureChangeListener) {
		this.material = material;
		this.material.setShaderString(material.getShaderString());
//		this.material.setShaderString(material.getShaderString() + "_copy");
		this.modelViewManager = modelViewManager;
		this.structureChangeListener = modelStructureChangeListener;
	}

	@Override
	public void undo() {
		modelViewManager.getModel().getMaterials().remove(material);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public void redo() {
		modelViewManager.getModel().addMaterial(material);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public String actionName() {
		return "add Material";
	}

}
