package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class AddMaterialAction implements UndoAction {
	private final Material material;
	private final ModelView modelViewManager;
	private final ModelStructureChangeListener structureChangeListener;

	public AddMaterialAction(final Material material,
	                         final ModelView modelViewManager,
	                         final ModelStructureChangeListener modelStructureChangeListener) {
		this.material = material;
		this.material.setShaderString(material.getShaderString());
//		this.material.setShaderString(material.getShaderString() + "_copy");
		this.modelViewManager = modelViewManager;
		this.structureChangeListener = modelStructureChangeListener;
	}

	@Override
	public UndoAction undo() {
		modelViewManager.getModel().getMaterials().remove(material);
		structureChangeListener.materialsListChanged();
		return this;
	}

	@Override
	public UndoAction redo() {
		modelViewManager.getModel().addMaterial(material);
		structureChangeListener.materialsListChanged();
		return this;
	}

	@Override
	public String actionName() {
		return "add Material";
	}

}
