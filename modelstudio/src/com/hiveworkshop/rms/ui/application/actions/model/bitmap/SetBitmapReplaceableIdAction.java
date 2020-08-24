package com.hiveworkshop.rms.ui.application.actions.model.bitmap;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.editor.model.Bitmap;

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
	public void undo() {
		bitmap.setReplaceableId(prevId);
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public void redo() {
		bitmap.setReplaceableId(newId);
		modelStructureChangeListener.texturesChanged();
	}

	@Override
	public String actionName() {
		return "change texture ReplaceableId";
	}
}
