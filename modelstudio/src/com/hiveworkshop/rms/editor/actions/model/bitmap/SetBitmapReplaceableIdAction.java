package com.hiveworkshop.rms.editor.actions.model.bitmap;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetBitmapReplaceableIdAction implements UndoAction {
	private final Bitmap bitmap;
	private final int prevId;
	private final int newId;
	private final ModelStructureChangeListener modelStructureChangeListener;

	public SetBitmapReplaceableIdAction(final Bitmap bitmap, final int prevId, final int newId,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.bitmap = bitmap;
		this.prevId = prevId;
		this.newId = newId;
		this.modelStructureChangeListener = modelStructureChangeListener;
	}

	@Override
	public UndoAction undo() {
		bitmap.setReplaceableId(prevId);
		modelStructureChangeListener.texturesChanged();
		return this;
	}

	@Override
	public UndoAction redo() {
		bitmap.setReplaceableId(newId);
		modelStructureChangeListener.texturesChanged();
		return this;
	}

	@Override
	public String actionName() {
		return "change texture ReplaceableId";
	}
}
