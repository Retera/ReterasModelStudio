package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetLayerFilterModeAction implements UndoAction {
	private final Layer layer;
	private final FilterMode prevFilterMode;
	private final FilterMode newFilterMode;
	private final ModelStructureChangeListener changeListener;

	public SetLayerFilterModeAction(Layer layer, FilterMode newFilterMode, ModelStructureChangeListener changeListener) {
		this.layer = layer;
		this.prevFilterMode = layer.getFilterMode();
		this.newFilterMode = newFilterMode;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		layer.setFilterMode(prevFilterMode);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		layer.setFilterMode(newFilterMode);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set Layer FilterMode";
	}

}
