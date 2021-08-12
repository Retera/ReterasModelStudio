package com.hiveworkshop.rms.editor.actions.model.bitmap;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class RemoveBitmapAction implements UndoAction {
	private final Bitmap bitmap;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener;

	public RemoveBitmapAction(Bitmap bitmap, EditableModel model, ModelStructureChangeListener changeListener) {
		this.bitmap = bitmap;
		this.model = model;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		model.add(bitmap);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		model.remove(bitmap);
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
