package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetLayerAlphaAction implements UndoAction {
	private final Layer layer;
	private final double prevFilterMode;
	private final double newFilterMode;
	private final ModelStructureChangeListener changeListener;

	public SetLayerAlphaAction(Layer layer, double newAlpha, ModelStructureChangeListener changeListener) {
		this.layer = layer;
		this.prevFilterMode = layer.getStaticAlpha();
		this.newFilterMode = newAlpha;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		layer.setStaticAlpha(prevFilterMode);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		layer.setStaticAlpha(newFilterMode);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set Layer Alpha";
	}
}
