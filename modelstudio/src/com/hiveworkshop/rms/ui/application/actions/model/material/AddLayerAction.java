package com.hiveworkshop.rms.ui.application.actions.model.material;

import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class AddLayerAction implements UndoAction {
	private final Material material;
	private final Layer layer;
	private final ModelStructureChangeListener structureChangeListener;

	public AddLayerAction(final Layer layer,
	                      final Material material,
	                      final ModelStructureChangeListener modelStructureChangeListener) {
		this.layer = layer;
		this.material = material;
		this.structureChangeListener = modelStructureChangeListener;
	}

	@Override
	public void undo() {
		material.getLayers().remove(layer);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public void redo() {
		material.getLayers().add(layer);
		structureChangeListener.materialsListChanged();
	}

	@Override
	public String actionName() {
		return "add Layer";
	}

}
