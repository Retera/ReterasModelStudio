package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetMaterialShaderStringAction implements UndoAction {
	private final Material material;
	private final String prevShader;
	private final String newShader;
	private final ModelStructureChangeListener modelStructureChangeListener;

	public SetMaterialShaderStringAction(final Material material, final String prevShader, final String newShader,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.material = material;
		this.prevShader = prevShader;
		this.newShader = newShader;
		this.modelStructureChangeListener = modelStructureChangeListener;
	}

	@Override
	public UndoAction undo() {
		material.setShaderString(prevShader);
		modelStructureChangeListener.texturesChanged();
		return this;
	}

	@Override
	public UndoAction redo() {
		material.setShaderString(newShader);
		modelStructureChangeListener.texturesChanged();
		return this;
	}

	@Override
	public String actionName() {
		return "set material Shader";
	}

}
