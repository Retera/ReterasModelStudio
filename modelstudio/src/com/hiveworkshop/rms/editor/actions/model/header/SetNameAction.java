package com.hiveworkshop.rms.editor.actions.model.header;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetNameAction implements UndoAction {
	private final String prevName;
	private final String newName;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener;

	public SetNameAction(String newName, EditableModel model, ModelStructureChangeListener changeListener) {
		this.prevName = model.getHeaderName();
		this.newName = newName;
		this.model = model;
		this.changeListener = changeListener;
	}

	@Override
	public SetNameAction undo() {
		model.setName(prevName);
		if (changeListener != null) {
			changeListener.headerChanged();
		}
		return this;
	}

	@Override
	public SetNameAction redo() {
		model.setName(newName);
		if (changeListener != null) {
			changeListener.headerChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Set Name To \"" + newName + "\"";
	}

}
