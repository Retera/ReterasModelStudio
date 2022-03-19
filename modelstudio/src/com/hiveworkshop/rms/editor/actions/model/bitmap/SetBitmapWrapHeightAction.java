package com.hiveworkshop.rms.editor.actions.model.bitmap;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetBitmapWrapHeightAction implements UndoAction {
	private final Bitmap bitmap;
	private final boolean prevState;
	private final boolean newState;
	private final ModelStructureChangeListener changeListener;

	public SetBitmapWrapHeightAction(Bitmap bitmap, boolean newState, ModelStructureChangeListener changeListener) {
		this.bitmap = bitmap;
		this.prevState = bitmap.isWrapHeight();
		this.newState = newState;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		bitmap.setWrapHeight(prevState);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		bitmap.setWrapHeight(newState);
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
