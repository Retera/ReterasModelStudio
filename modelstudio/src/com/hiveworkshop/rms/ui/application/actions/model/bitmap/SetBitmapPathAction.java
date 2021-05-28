package com.hiveworkshop.rms.ui.application.actions.model.bitmap;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

public class SetBitmapPathAction implements UndoAction {
	private final Bitmap bitmap;
	private final String prevPath;
	private final String newPath;
	private final ModelStructureChangeListener modelStructureChangeListener;

	public SetBitmapPathAction(final Bitmap bitmap, final String prevPath, final String newPath,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.bitmap = bitmap;
		this.prevPath = prevPath;
		this.newPath = newPath;
		this.modelStructureChangeListener = modelStructureChangeListener;
	}

	@Override
	public UndoAction undo() {
		bitmap.setPath(prevPath);
		modelStructureChangeListener.texturesChanged();
		return this;
	}

	@Override
	public UndoAction redo() {
		bitmap.setPath(newPath);
		modelStructureChangeListener.texturesChanged();
		return this;
	}

	@Override
	public String actionName() {
		return "change texture Path";
	}
}
