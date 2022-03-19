package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class AddLayerAction implements UndoAction {
	private final Material material;
	private final Layer layer;
	private final ModelStructureChangeListener changeListener;

	public AddLayerAction(Layer layer, Material material, ModelStructureChangeListener changeListener) {
		this.layer = layer;
		this.material = material;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		material.removeLayer(layer);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		material.addLayer(layer);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "add Layer";
	}

}
