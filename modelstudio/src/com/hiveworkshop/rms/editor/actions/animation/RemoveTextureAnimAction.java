package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.TextureAnim;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class RemoveTextureAnimAction implements UndoAction {
	private final EditableModel model;
	private final TextureAnim textureAnim;
	private final ModelStructureChangeListener changeListener;

	public RemoveTextureAnimAction(TextureAnim textureAnim, EditableModel model, ModelStructureChangeListener changeListener) {
		this.model = model;
		this.textureAnim = textureAnim;

		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		model.add(textureAnim);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		model.remove(textureAnim);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Remove TextureAnim";
	}
}
