package com.hiveworkshop.rms.ui.application.actions.model.material;

import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class RemoveLayerAction implements UndoAction {
	private final Material material;
	private final Layer layer;
	private final ModelStructureChangeListener structureChangeListener;

	public RemoveLayerAction(final Layer layer,
	                         final Material material,
	                         final ModelStructureChangeListener modelStructureChangeListener) {
		this.layer = layer;
		this.material = material;
//		this.material.setShaderString(layer.getShaderString() + "_copy");
		this.structureChangeListener = modelStructureChangeListener;
	}

	@Override
	public void undo() {
		material.getLayers().add(layer);
//		modelViewManager.getModel().getMaterials().remove(layer);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public void redo() {
		material.getLayers().remove(layer);
//		modelViewManager.getModel().addMaterial(layer);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public String actionName() {
		return "remove Layer";
	}

}
