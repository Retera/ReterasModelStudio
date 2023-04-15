package com.hiveworkshop.rms.editor.actions.model.bitmap;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetBitmapWrapModeAction implements UndoAction {
	private final Bitmap bitmap;
	private final Bitmap.WrapFlag flag;
	private final boolean set;
	private final boolean oldState;
	private final ModelStructureChangeListener changeListener;

	public SetBitmapWrapModeAction(Bitmap bitmap, Bitmap.WrapFlag flag, boolean set, ModelStructureChangeListener changeListener) {
		this.bitmap = bitmap;
		this.flag = flag;
		this.set = set;
		this.oldState = bitmap.isFlagSet(flag);
		this.changeListener = changeListener;
	}

	@Override
	public SetBitmapWrapModeAction undo() {
		bitmap.setFlag(flag, oldState);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public SetBitmapWrapModeAction redo() {
		bitmap.setFlag(flag, set);
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Change Texture Wrap Mode";
	}
}
