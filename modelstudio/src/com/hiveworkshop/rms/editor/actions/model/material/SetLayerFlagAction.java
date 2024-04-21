package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetLayerFlagAction implements UndoAction {
	private final Layer layer;
	private final Layer.Flag flag;
	private final boolean set;
	private final boolean oldState;
	private final ModelStructureChangeListener changeListener;

	public SetLayerFlagAction(Layer layer, Layer.Flag flag, boolean set, ModelStructureChangeListener changeListener) {
		this.layer = layer;
		this.flag = flag;
		this.set = set;
		this.oldState = layer.isFlagSet(flag);
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		layer.setFlag(flag, oldState);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		layer.setFlag(flag, set);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Toggled Layer " + flag;
	}
}
