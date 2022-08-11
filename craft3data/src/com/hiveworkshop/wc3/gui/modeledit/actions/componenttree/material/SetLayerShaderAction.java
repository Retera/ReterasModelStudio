package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.LayerShader;

public class SetLayerShaderAction implements UndoAction {
	private final Layer layer;
	private final LayerShader prevShader;
	private final LayerShader newShader;
	private final ModelStructureChangeListener modelStructureChangeListener;

	public SetLayerShaderAction(final Layer layer, final LayerShader prevShader, final LayerShader newShader,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.layer = layer;
		this.prevShader = prevShader;
		this.newShader = newShader;
		this.modelStructureChangeListener = modelStructureChangeListener;
	}

	@Override
	public void undo() {
		layer.setLayerShader(prevShader);
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public void redo() {
		layer.setLayerShader(newShader);
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public String actionName() {
		return "set layer Shader";
	}

}
