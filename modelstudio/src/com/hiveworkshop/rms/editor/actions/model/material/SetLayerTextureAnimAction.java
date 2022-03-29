package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.TextureAnim;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetLayerTextureAnimAction implements UndoAction {
	private final Layer layer;
	private final TextureAnim prevTVertAnim;
	private final TextureAnim newTVertAnim;
	private final ModelStructureChangeListener changeListener;

	public SetLayerTextureAnimAction(Layer layer, TextureAnim newTVertAnim, ModelStructureChangeListener changeListener) {
		this.layer = layer;
		this.prevTVertAnim = layer.getTextureAnim();
		this.newTVertAnim = newTVertAnim;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		layer.setTextureAnim(prevTVertAnim);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		layer.setTextureAnim(newTVertAnim);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set Layer TVertAnim";
	}
}
