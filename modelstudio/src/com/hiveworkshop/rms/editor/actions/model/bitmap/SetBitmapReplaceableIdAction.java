package com.hiveworkshop.rms.editor.actions.model.bitmap;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetBitmapReplaceableIdAction implements UndoAction {
	private final Bitmap bitmap;
	private final int prevId;
	private final int newId;
	private final ModelStructureChangeListener changeListener;

	public SetBitmapReplaceableIdAction(Bitmap bitmap, int newId, ModelStructureChangeListener changeListener) {
		this.bitmap = bitmap;
		this.prevId = bitmap.getReplaceableId();
		this.newId = newId;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		bitmap.setReplaceableId(prevId);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		bitmap.setReplaceableId(newId);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "change texture ReplaceableId";
	}
}
