package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetMaterialShaderStringAction implements UndoAction {
	private final Material material;
	private final String prevShader;
	private final String newShader;
	private final ModelStructureChangeListener changeListener;

	public SetMaterialShaderStringAction(Material material, String newShader, ModelStructureChangeListener changeListener) {
		this.material = material;
		this.prevShader = material.getShaderString();
		this.newShader = newShader;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		material.setShaderString(prevShader);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		material.setShaderString(newShader);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set material Shader";
	}

}
