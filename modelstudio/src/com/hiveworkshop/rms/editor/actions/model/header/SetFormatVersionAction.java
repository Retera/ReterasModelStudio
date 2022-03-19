package com.hiveworkshop.rms.editor.actions.model.header;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetFormatVersionAction implements UndoAction {
	private final int prevVersion;
	private final int newVersion;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener;

	public SetFormatVersionAction(int newVersion, EditableModel model, ModelStructureChangeListener changeListener) {
		this.prevVersion = model.getFormatVersion();
		this.newVersion = newVersion;
		this.model = model;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		model.setFormatVersion(prevVersion);
		if (changeListener != null) {
			changeListener.headerChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		model.setFormatVersion(newVersion);
		if (changeListener != null) {
			changeListener.headerChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "set FormatVersion to " + newVersion;
	}

}
