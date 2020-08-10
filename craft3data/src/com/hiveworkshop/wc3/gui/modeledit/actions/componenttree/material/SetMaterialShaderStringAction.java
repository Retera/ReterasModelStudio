package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Material;

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
