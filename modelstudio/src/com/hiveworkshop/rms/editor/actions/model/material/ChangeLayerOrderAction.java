package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class ChangeLayerOrderAction implements UndoAction {
	private final Material material;
	private final Layer layer;
	private final ModelStructureChangeListener changeListener;
	private final int newIndex;
	private final int oldIndex;

	public ChangeLayerOrderAction(Material material, Layer layer, int newIndex, ModelStructureChangeListener changeListener) {
		this.material = material;
		this.layer = layer;
		this.newIndex = newIndex;
		this.oldIndex = material.getLayers().indexOf(layer);
		this.changeListener = changeListener;
	}

	public ChangeLayerOrderAction(Material material, Layer layer, boolean moveUp, ModelStructureChangeListener changeListener) {
		this.material = material;
		this.layer = layer;
		this.oldIndex = material.getLayers().indexOf(layer);
		this.newIndex = moveUp ? oldIndex - 1 : oldIndex + 1;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		moveLayer(oldIndex);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		moveLayer(newIndex);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	private void moveLayer(int index) {
		if (index >= 0 && index <= material.getLayers().size() - 1) {
			material.removeLayer(layer);
			material.addLayer(index, layer);
		}
	}

	@Override
	public String actionName() {
		return "Change layer order";
	}
}
