package com.hiveworkshop.rms.editor.actions.model.header;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetHeaderExtentsAction implements UndoAction {
	private final ExtLog prevExtLog;
	private final ExtLog newExtLog;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener;

	public SetHeaderExtentsAction(ExtLog newExtLog, EditableModel model, ModelStructureChangeListener changeListener) {
		this.prevExtLog = model.getExtents();
		this.newExtLog = newExtLog;
		this.model = model;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		model.setExtents(prevExtLog);
		if (changeListener != null) {
			changeListener.headerChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		model.setExtents(newExtLog);
		if (changeListener != null) {
			changeListener.headerChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set extents";
	}
}
