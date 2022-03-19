package com.hiveworkshop.rms.editor.actions.model.bitmap;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetBitmapPathAction implements UndoAction {
	private final Bitmap bitmap;
	private final String prevPath;
	private final String newPath;
	private final ModelStructureChangeListener changeListener;

	public SetBitmapPathAction(Bitmap bitmap, String newPath, ModelStructureChangeListener changeListener) {
		this.bitmap = bitmap;
		this.prevPath = bitmap.getPath();
		this.newPath = newPath;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		bitmap.setPath(prevPath);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		bitmap.setPath(newPath);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "change texture Path";
	}
}
