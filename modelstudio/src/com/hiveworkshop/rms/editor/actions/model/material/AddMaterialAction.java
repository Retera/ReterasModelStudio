package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class AddMaterialAction implements UndoAction {
	private final Material material;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener;

	public AddMaterialAction(final Material material,
	                         final EditableModel model,
	                         final ModelStructureChangeListener modelStructureChangeListener) {
		this.material = material;
		this.material.setShaderString(material.getShaderString());
//		this.material.setShaderString(material.getShaderString() + "_copy");
		this.model = model;
		this.changeListener = modelStructureChangeListener;
	}

	@Override
	public UndoAction undo() {
		model.getMaterials().remove(material);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		model.addMaterial(material);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "add Material";
	}

}
