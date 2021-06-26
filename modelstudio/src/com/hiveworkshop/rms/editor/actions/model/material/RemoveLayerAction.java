package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class RemoveLayerAction implements UndoAction {
	private final Material material;
	private final Layer layer;
	private final ModelStructureChangeListener changeListener;

	public RemoveLayerAction(Layer layer, Material material, ModelStructureChangeListener changeListener) {
		this.layer = layer;
		this.material = material;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		material.addLayer(layer);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		material.removeLayer(layer);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "remove Layer";
	}

}
