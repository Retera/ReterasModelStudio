package com.hiveworkshop.rms.ui.application.actions.model.material;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.editor.model.Material;

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
	public void undo() {
		material.setShaderString(prevShader);
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public void redo() {
		material.setShaderString(newShader);
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public String actionName() {
		return "set material Shader";
	}

}
