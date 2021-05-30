package com.hiveworkshop.rms.editor.actions.model.bitmap;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetBitmapWrapHeightAction implements UndoAction {
	private final Bitmap bitmap;
	private final boolean prevState;
	private final boolean newState;
	private final ModelStructureChangeListener modelStructureChangeListener;

	public SetBitmapWrapHeightAction(final Bitmap bitmap, final boolean prevState, final boolean newState,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.bitmap = bitmap;
		this.prevState = prevState;
		this.newState = newState;
		this.modelStructureChangeListener = modelStructureChangeListener;
	}

	@Override
	public UndoAction undo() {
		bitmap.setWrapHeight(prevState);
		modelStructureChangeListener.texturesChanged();
		return this;
	}

	@Override
	public UndoAction redo() {
		bitmap.setWrapHeight(newState);
		modelStructureChangeListener.texturesChanged();
		return this;
	}

	@Override
	public String actionName() {
		return "change texture ReplaceableId";
	}
}
