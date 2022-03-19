package com.hiveworkshop.rms.editor.actions.model.header;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetBlendTimeAction implements UndoAction {
	private final int prevBlendTime;
	private final int newBlendTime;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener;

	public SetBlendTimeAction(int newBlendTime, EditableModel model, ModelStructureChangeListener changeListener) {
		this.prevBlendTime = model.getBlendTime();
		this.newBlendTime = newBlendTime;
		this.model = model;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		model.setBlendTime(prevBlendTime);
		if (changeListener != null) {
			changeListener.headerChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		model.setBlendTime(newBlendTime);
		if (changeListener != null) {
			changeListener.headerChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set BlendTime to " + newBlendTime;
	}

}
